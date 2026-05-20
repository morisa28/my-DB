#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "$ROOT/scripts/lib/env.sh"

load_default_env "$ROOT"

MYSQL_DATABASE="${MYSQL_DATABASE:-mall_db}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
DB_HOST="${DB_HOST:-127.0.0.1}"
DB_USERNAME="${DB_USERNAME:-root}"
DB_PASSWORD="${DB_PASSWORD:-${MYSQL_ROOT_PASSWORD:-}}"
BACKUP_DIR="${BACKUP_DIR:-$ROOT/backups/db}"
RETENTION_DAYS="${RETENTION_DAYS:-14}"

validate_mysql_database_name "$MYSQL_DATABASE"
require_command gzip

timestamp="$(date +%Y%m%d_%H%M%S)"
backup_file="$BACKUP_DIR/${MYSQL_DATABASE}_${timestamp}.sql.gz"
tmp_file="$backup_file.tmp"

mkdir -p "$BACKUP_DIR"

cleanup() {
    rm -f "$tmp_file"
}
trap cleanup EXIT

if docker_compose_service_running mysql; then
    docker compose exec -T mysql sh -c 'exec mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --single-transaction --routines --triggers "$MYSQL_DATABASE"' \
        | gzip -c > "$tmp_file"
else
    require_command mysqldump

    if [[ -z "$DB_PASSWORD" ]]; then
        echo "DB_PASSWORD or MYSQL_ROOT_PASSWORD is required for local mysqldump backup." >&2
        exit 1
    fi

    MYSQL_PWD="$DB_PASSWORD" mysqldump \
        -h "$DB_HOST" \
        -P "$MYSQL_PORT" \
        -u "$DB_USERNAME" \
        --single-transaction \
        --routines \
        --triggers \
        "$MYSQL_DATABASE" \
        | gzip -c > "$tmp_file"
fi

mv "$tmp_file" "$backup_file"
trap - EXIT

if [[ "$RETENTION_DAYS" =~ ^[0-9]+$ ]]; then
    find "$BACKUP_DIR" -type f -name "${MYSQL_DATABASE}_*.sql.gz" -mtime +"$RETENTION_DAYS" -delete
fi

echo "Database backup created: $backup_file"
