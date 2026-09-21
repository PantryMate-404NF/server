#!/usr/bin/env bash
# 실행: bash scripts/run-local.sh  (또는 ./scripts/run-local.sh)
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

LOG_DIR="$ROOT_DIR/logs"
mkdir -p "$LOG_DIR"

if [ ! -f .env ]; then
  echo ".env 파일이 없습니다. .env.example을 참고해서 먼저 만들어주세요." >&2
  exit 1
fi

set -a
source .env
set +a

# Git Bash(MSYS)가 '/'로 시작하는 값(JWT_SECRET 등)을 POSIX 경로로 오인해 변환하는 문제 방지.
# docs/architecture/05_local_dev_troubleshooting.md 참고.
export MSYS_NO_PATHCONV=1
export MSYS2_ARG_CONV_EXCL="*"

echo "postgres/redis 컨테이너 기동 확인..."
docker compose up -d postgres redis

health() {
  # MSYS_NO_PATHCONV이 켜진 상태에서는 curl의 `-o /dev/null` 인자가 그대로 전달되어
  # Windows에서 NUL로 변환되지 못해 curl이 exit 23을 내므로, -o 없이 응답 본문 뒤에
  # http_code를 개행으로 붙여 받은 다음 마지막 줄만 취하는 방식으로 우회한다.
  local raw code
  raw=$(curl -s -w $'\n%{http_code}' "http://localhost:$1/actuator/health" 2>/dev/null) || raw=""
  code=$(printf '%s' "$raw" | tail -n1)
  printf '%s' "${code:-000}"
}

port_in_use() {
  netstat -ano 2>/dev/null | grep -q ":$1 .*LISTENING"
}

start_service() {
  local name="$1" gradle_target="$2" port="$3"

  if port_in_use "$port"; then
    echo "[$name] 이미 $port 포트를 사용 중인 프로세스가 있음 — 건너뜀."
    return
  fi

  echo "[$name] 기동 중... (로그: logs/$name.log)"
  nohup ./gradlew "$gradle_target" > "$LOG_DIR/$name.log" 2>&1 &
  echo $! > "$LOG_DIR/$name.pid"
}

start_service "user-service" ":services:user-service:bootRun" 8081
start_service "gateway-service" ":platform:gateway-service:bootRun" 8080
start_service "order-payment-service" ":services:order-payment-service:bootRun" 8084

echo
echo "기동 확인 중 (최대 90초 대기)..."
for i in $(seq 1 45); do
  u=$(health 8081); g=$(health 8080); p=$(health 8084)
  if [ "$u" = "200" ] && [ "$g" = "200" ] && [ "$p" = "200" ]; then
    echo "모두 기동 완료 — gateway:8080 user:8081 order-payment:8084"
    exit 0
  fi
  sleep 2
done

echo "90초 안에 기동을 확인하지 못했습니다. logs/ 안의 로그를 확인해주세요." >&2
echo "현재 상태: user=$u gateway=$g pantry=$p" >&2
exit 1