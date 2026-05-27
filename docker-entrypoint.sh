#!/bin/sh
set -e

# Convert Railway/Docker DATABASE_URL (postgresql://user:pass@host:port/db) to JDBC format
if [ -n "$DATABASE_URL" ]; then
    DB_WITHOUT_SCHEME="${DATABASE_URL#postgresql://}"
    DB_USERINFO="${DB_WITHOUT_SCHEME%%@*}"
    DB_HOSTPATH="${DB_WITHOUT_SCHEME#*@}"
    DB_USER="${DB_USERINFO%%:*}"
    DB_PASS="${DB_USERINFO#*:}"

    export SPRING_DATASOURCE_URL="jdbc:postgresql://${DB_HOSTPATH}?sslmode=prefer"
    export SPRING_DATASOURCE_USERNAME="$DB_USER"
    export SPRING_DATASOURCE_PASSWORD="$DB_PASS"
fi

exec java -jar app.jar
