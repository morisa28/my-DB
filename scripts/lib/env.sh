#!/usr/bin/env bash

load_env_file() {
    local env_file="$1"

    [[ -f "$env_file" ]] || return 0

    while IFS= read -r line || [[ -n "$line" ]]; do
        line="${line%$'\r'}"
        [[ -z "${line//[[:space:]]/}" ]] && continue
        [[ "$line" =~ ^[[:space:]]*# ]] && continue
        [[ "$line" == *"="* ]] || continue

        local key="${line%%=*}"
        local value="${line#*=}"

        [[ "$key" =~ ^[A-Za-z_][A-Za-z0-9_]*$ ]] || continue

        if [[ "$value" == \"*\" && "$value" == *\" ]]; then
            value="${value:1:${#value}-2}"
        elif [[ "$value" == \'*\' && "$value" == *\' ]]; then
            value="${value:1:${#value}-2}"
        fi

        export "$key=$value"
    done < "$env_file"
}

load_default_env() {
    local root="$1"
    local env_file="${ENV_FILE:-$root/.env}"

    if [[ -f "$env_file" ]]; then
        load_env_file "$env_file"
    elif [[ -f "$root/.env.example" ]]; then
        load_env_file "$root/.env.example"
    fi
}

require_command() {
    local command_name="$1"

    if ! command -v "$command_name" >/dev/null 2>&1; then
        echo "Required command not found: $command_name" >&2
        exit 1
    fi
}

validate_mysql_database_name() {
    local database_name="$1"

    if [[ ! "$database_name" =~ ^[A-Za-z0-9_]+$ ]]; then
        echo "Invalid MySQL database name: $database_name" >&2
        exit 1
    fi
}

docker_compose_service_running() {
    local service_name="$1"

    docker compose ps --services --filter status=running 2>/dev/null | grep -qx "$service_name"
}
