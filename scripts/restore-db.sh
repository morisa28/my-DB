#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "$ROOT/scripts/lib/env.sh"

load_default_env "$ROOT"

if [[ $# -ne 1 ]]; then
    echo "Usage: RESTORE_DATABASE=mall_db_restore bash scripts/restore-db.sh <backup.sql|backup.sql.gz>" >&2
    exit 1
fi

backup_file="$1"
MYSQL_DATABASE="${MYSQL_DATABASE:-mall_db}"
RESTORE_DATABASE="${RESTORE_DATABASE:-${MYSQL_DATABASE}_restore}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
DB_HOST="${DB_HOST:-127.0.0.1}"
DB_USERNAME="${DB_USERNAME:-root}"
DB_PASSWORD="${DB_PASSWORD:-${MYSQL_ROOT_PASSWORD:-}}"

[[ -f "$backup_file" ]] || {
    echo "Backup file not found: $backup_file" >&2
    exit 1
}

validate_mysql_database_name "$MYSQL_DATABASE"
validate_mysql_database_name "$RESTORE_DATABASE"

if [[ "$RESTORE_DATABASE" == "$MYSQL_DATABASE" && "${CONFIRM_RESTORE:-}" != "I_UNDERSTAND" ]]; then
    echo "Refusing to restore over MYSQL_DATABASE=$MYSQL_DATABASE without CONFIRM_RESTORE=I_UNDERSTAND." >&2
    echo "Use the default restore database for verification, or set CONFIRM_RESTORE only after taking a fresh backup." >&2
    exit 1
fi

decompress_backup() {
    case "$backup_file" in
        *.gz)
            require_command gzip
            gzip -dc "$backup_file"
            ;;
        *)
            cat "$backup_file"
            ;;
    esac
}

if docker_compose_service_running mysql; then
    docker compose exec -T -e RESTORE_DATABASE="$RESTORE_DATABASE" mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -e "CREATE DATABASE IF NOT EXISTS \`$RESTORE_DATABASE\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci"'
    decompress_backup | docker compose exec -T -e RESTORE_DATABASE="$RESTORE_DATABASE" mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" "$RESTORE_DATABASE"'
else
    require_command mysql

    if [[ -z "$DB_PASSWORD" ]]; then
        echo "DB_PASSWORD or MYSQL_ROOT_PASSWORD is required for local mysql restore." >&2
        exit 1
    fi

    MYSQL_PWD="$DB_PASSWORD" mysql \
        -h "$DB_HOST" \
        -P "$MYSQL_PORT" \
        -u "$DB_USERNAME" \
        -e "CREATE DATABASE IF NOT EXISTS \`$RESTORE_DATABASE\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci"

    decompress_backup | MYSQL_PWD="$DB_PASSWORD" mysql \
        -h "$DB_HOST" \
        -P "$MYSQL_PORT" \
        -u "$DB_USERNAME" \
        "$RESTORE_DATABASE"
fi

echo "Database backup restored into: $RESTORE_DATABASE"
