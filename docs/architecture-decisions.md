# PantryMate Backend — Architecture Decisions

프로젝트 초기 구조 설계와 그 배경을 정리한 문서입니다.

---

## 1. 디렉토리 구조 (Monorepo)

여러 마이크로서비스를 하나의 저장소에서 관리하는 **Monorepo** 구조를 채택했습니다.

```
pantrymate-backend/
├── docs/                     // 아키텍처/컨벤션 문서
├── modules/
│   └── common/                // 공통 예외 처리, DTO — 라이브러리 (실행 불가)
├── platform/
│   └── gateway-service/       // Spring Cloud Gateway — 인증/라우팅 전담
├── services/
│   └── user-service/          // 도메인 서비스 (신규 서비스는 이 아래 추가)
├── settings.gradle
└── build.gradle
```

새 도메인 서비스가 필요하면 `services/xxx-service` 형태로 추가하고, `settings.gradle`의 서비스 목록에 이름만 추가하면 됩니다.

각 서비스 내부는 **Layered Architecture**를 따릅니다.

```
presentation → application → domain → infrastructure
```

- `presentation`: Controller, 요청/응답 DTO
- `application`: 서비스 로직, 트랜잭션 경계
- `domain`: 엔티티, 도메인 규칙
- `infrastructure`: JPA repository, 외부 API 클라이언트

---

## 2. 서비스 디스커버리: Eureka 미사용

k8s 환경에서 운영하므로 별도의 서비스 디스커버리 서버(Eureka)를 두지 않습니다. 서비스 간 라우팅은 **k8s Service DNS**로 대체합니다.

```
http://{service-name}.{namespace}.svc.cluster.local:{port}
```

Gateway의 라우팅 설정(`application.yml`)에서도 이 DNS를 `uri`로 직접 사용합니다.

---

## 3. API Gateway

**Spring Cloud Gateway**를 두고 그 앞단은 인프라 담당 영역(ALB → Ingress)에 맡깁니다.

```
Client → ALB/Ingress (TLS termination, 기본 라우팅) → Spring Cloud Gateway (Pod) → 각 서비스 (ClusterIP)
```

Gateway가 담당하는 것:
- **JWT 검증**: `oauth2ResourceServer` 로 토큰 검증
- **클레임 전달**: 검증된 사용자 정보를 `X-User-Id` 헤더로 다운스트림에 전달 → 각 서비스는 헤더만 신뢰, 토큰 재검증 없음
- **경로 기반 라우팅**: `/api/users/**` → user-service 등

인프라(ALB/Ingress)가 담당하는 것: 외부 진입점, TLS, 기본 도메인 라우팅. 이 경계는 **인프라팀 결정 사항이 아니라 이미 확정된 것으로 간주** — 백엔드는 Gateway 뒤쪽만 책임집니다.

---

## 4. 서비스 간 통신

초기 단계는 **동기 통신(OpenFeign / RestClient)** 위주로 시작합니다. 이벤트 기반 비동기 통신(RabbitMQ/Kafka + Outbox 패턴)은 아직 도입하지 않았습니다.

> **Outbox 패턴 도입 시점**: 서비스 간 결합이 느슨해도 되는 관계(알림, 이벤트 전파 등)가 명확해지고, 이벤트 유실이 비즈니스적으로 허용 안 되는 지점이 생기면 그때 검토합니다. 도입 시 `modules/messaging` 같은 별도 공통 모듈 + Spring Modulith Outbox + 메시지 브로커 조합을 참고할 수 있습니다 (현재는 미적용).

---

## 5. 빌드 / 버전

| 항목 | 값 |
|---|---|
| 빌드 도구 | Gradle (Groovy DSL), multi-module |
| 언어 | Java 25 (LTS) |
| Spring Boot | 4.1.0 |
| Spring Cloud | 2025.1.2 |

버전 관리는 루트 `build.gradle`의 BOM(`dependencyManagement`)으로 일괄 적용되며, 각 서비스는 버전을 명시하지 않습니다.

`modules/common`처럼 실행 가능한 서버가 아닌 라이브러리 모듈에는 Spring Boot 플러그인을 적용하지 않습니다 (루트 `build.gradle`에서 `services:`, `platform:` 경로만 필터링해 적용).

---

## 6. DB

서비스별로 **자신의 DB를 소유**하며 다른 서비스의 DB에 직접 접근하지 않습니다 (Database per Service).

---

## 참고

이 구조는 유사 프로젝트([bds_backend](https://github.com/KT-Cloud-2-BDS/bds_backend))의 검증된 패턴을 참고해 PantryMate 상황(k8s 환경, 초기 단계 팀 규모)에 맞게 조정한 것입니다. Eureka 제외, 비동기/Outbox 보류가 주요 차이점입니다.
