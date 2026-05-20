#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "$ROOT/scripts/lib/env.sh"

load_default_env "$ROOT"

UPLOAD_DIR="${UPLOAD_DIR:-/app/uploads}"
UPLOAD_SOURCE_DIR="${UPLOAD_SOURCE_DIR:-}"
BACKUP_DIR="${BACKUP_DIR:-$ROOT/backups/uploads}"
RETENTION_DAYS="${RETENTION_DAYS:-14}"

require_command tar
require_command gzip

timestamp="$(date +%Y%m%d_%H%M%S)"
backup_file="$BACKUP_DIR/uploads_${timestamp}.tar.gz"
tmp_file="$backup_file.tmp"

mkdir -p "$BACKUP_DIR"

cleanup() {
    rm -f "$tmp_file"
}
trap cleanup EXIT

if docker_compose_service_running backend; then
    docker compose exec -T backend sh -c 'upload_dir="${UPLOAD_DIR:-/app/uploads}"; [ -d "$upload_dir" ] && tar -C "$upload_dir" -czf - .' > "$tmp_file"
elif [[ -n "$UPLOAD_SOURCE_DIR" && -d "$UPLOAD_SOURCE_DIR" ]]; then
    tar -C "$UPLOAD_SOURCE_DIR" -czf "$tmp_file" .
elif [[ -d "$UPLOAD_DIR" ]]; then
    tar -C "$UPLOAD_DIR" -czf "$tmp_file" .
else
    echo "Upload directory not found. Start the backend container or set UPLOAD_SOURCE_DIR to a local upload path." >&2
    exit 1
fi

mv "$tmp_file" "$backup_file"
trap - EXIT

if [[ "$RETENTION_DAYS" =~ ^[0-9]+$ ]]; then
    find "$BACKUP_DIR" -type f -name "uploads_*.tar.gz" -mtime +"$RETENTION_DAYS" -delete
fi

echo "Upload backup created: $backup_file"
