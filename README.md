# 🥬 PantryMate

재고를 아는 커머스, 구매 이력이 곧 재고 데이터가 되는 B2C 신선식품 플랫폼
PantryMate 백엔드 팀입니다.

<br>

## 👥 **팀원 소개**
| Github | [<img src="https://avatars.githubusercontent.com/yS2h" width="130px;">](https://github.com/yS2h) | [<img src="https://avatars.githubusercontent.com/jeonggugo" width="130px;">](https://github.com/jeonggugo) |
|---|---|---|

| 이름 | 담당 도메인 | 주요 구현 내용 |
| --- | --- | --- |
| 정국 | 상품 / 장바구니·주문·결제 | 상품 등록·조회, 카테고리, 장바구니 CRUD, 주문서 작성·조회·취소, 토스페이먼츠 결제 승인·취소·정합성 재조회 |
| 수현 | 계정 및 회원 | 회원가입, 로그인/JWT, 배송지·식이 설정 관리 | 알림 | 인앱 알림함, FCM 푸시 발송, 알림 수신 설정 |
| 수현, 정국 | 스마트 팬트리 | 팬트리 등록, 레시피 기반 갭 분석, AI 팀 레시피 추천 중계 |
<br>

## 🛠 기술 스택

| 분류 | 기술 |
| --- | --- |
| Language | Java |
| Framework | Spring Boot (Gradle 멀티모듈 MSA) |
| Database | PostgreSQL (서비스별 분리, Database-per-Service) |
| Messaging | Redis Streams (서비스 간 이벤트 처리) |
| Authentication | JWT with RTR, Spring Cloud Gateway 기반 중앙 인증(Token Relay) |
| Payment | 토스페이먼츠 (카드 결제) |
| Infra & DevOps | Docker, GitHub Actions |

## 🏗 서비스 구조

```
패키지 구조 확정시  업데이트
```

각 도메인은 `domain / application / infrastructure / presentation` 4계층 구조를 따릅니다. 서비스 간 데이터는 FK 없이 논리적 참조값으로만 연결되며, 필요 시 동기 REST 또는 Redis Streams 이벤트로 통신합니다.

## 💻 설치 및 실행 방법

### 1. 요구 사항

- Java (프로젝트 버전에 맞게 설정)
- Build Tool: Gradle
- Database: PostgreSQL
- Redis (이벤트 처리 및 캐싱)

### 2. 환경 변수 설정

각 서비스 루트에 `.env` 또는 `application.yml`에 아래 값을 설정합니다.

```
# Database (PostgreSQL)
DB_URL=jdbc:postgresql://localhost:5432/aipantry
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password

# 토스페이먼츠 시크릿 키
TOSS_SECRET_KEY=toss_secret_key

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# 아래 내용은 추가될 시 업데이트
```

⚠️ 시크릿 키나 DB 비밀번호가 포함된 `.env` 파일은 절대 GitHub에 커밋되지 않도록 주의하세요 (`.gitignore` 확인).


## 🚀 주요 기능 및 API

| 도메인 | 핵심 기능 | 확인 방법 (API) |
| --- | --- | --- |
| 상품 (Product) | 상품 등록, 목록/상세 조회 | 
| 카테고리 (Category) | 1차·2차 계층 카테고리 목록 조회 |
| 장바구니 (Cart) | 담기·조회·수량변경·삭제 | 
| 주문 (Order) | 주문서 작성, 목록/상세 조회, 취소 요청 | 
| 결제 (Payment) | 결제 생성·승인·취소/환불, 상태 재조회 | 

## 🤝 기여 방법 및 규칙

### 1. 브랜치 전략

- `main` : 상용 배포 브랜치
- `feature/{번호}-{도메인}` : 단위 기능 개발 브랜치 (예: `feature/3-product`)

### 2. 커밋 컨벤션

- `feat:` 새로운 기능 추가
- `fix:` 버그 수정
- `docs:` 문서 수정 (README 등)
- `refactor:` 코드 리팩토링 (기능 변화 없음)

## 🔗 공유 문서

| 문서 분류 | 설명 |
| --- | --- |
ex) | 프로젝트 기획서 | AI Pantry 기획안 및 요구사항 명세서 |
