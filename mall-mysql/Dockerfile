FROM mysql:8.0

COPY mall-backend/src/main/resources/sql/schema.sql /docker-entrypoint-initdb.d/01-schema.sql
COPY mall-backend/src/main/resources/sql/data.sql /docker-entrypoint-initdb.d/02-data.sql
