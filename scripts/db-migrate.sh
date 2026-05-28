#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

if [[ -x "${ROOT_DIR}/mvnw" ]]; then
  MVN_CMD=("${ROOT_DIR}/mvnw")
elif command -v mvn >/dev/null 2>&1; then
  MVN_CMD=(mvn)
else
  echo "Maven executable not found. Install Maven or add ./mvnw before running database migrations." >&2
  exit 1
fi

MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-root}"

COMMON_FLYWAY_ARGS=(
  "-Dflyway.user=${MYSQL_USER}"
  "-Dflyway.password=${MYSQL_PASSWORD}"
  "-Dflyway.baselineOnMigrate=true"
  "-Dflyway.baselineVersion=1"
  "-Dflyway.baselineDescription=Current schema before Flyway adoption"
  "-Dflyway.validateOnMigrate=true"
  "-Dflyway.cleanDisabled=true"
  "-Dflyway.outOfOrder=false"
  "-Dflyway.connectRetries=10"
)

migrate_service() {
  local module_path="$1"
  local database="$2"
  local migration_path="${ROOT_DIR}/${module_path}/src/main/resources/db/migration"
  local jdbc_url="jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${database}?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false"

  echo "Migrating ${database} from ${migration_path}"
  "${MVN_CMD[@]}" -f "${ROOT_DIR}/pom.xml" -pl "${module_path}" \
    "-Dflyway.url=${jdbc_url}" \
    "-Dflyway.locations=filesystem:${migration_path}" \
    "${COMMON_FLYWAY_ARGS[@]}" \
    flyway:migrate
}

migrate_service "EmiyaOJ-Auth/auth-service" "emiya_oj_auth"
migrate_service "EmiyaOJ-Problem/problem-service" "emiya_oj_problem"
migrate_service "EmiyaOJ-Judge/judge-service" "emiya_oj_judge"
migrate_service "EmiyaOJ-Blog/blog-service" "emiya_oj_blog"
