#!/bin/sh
set -e

# Convert Railway's DATABASE_URL (postgresql://...) to JDBC format
if [ -n "$DATABASE_URL" ]; then
    # Extract components from postgresql://user:pass@host:port/db
    DB_WITHOUT_SCHEME="${DATABASE_URL#postgresql://}"
    DB_USERINFO="${DB_WITHOUT_SCHEME%%@*}"
    DB_HOSTPATH="${DB_WITHOUT_SCHEME#*@}"
    DB_USER="${DB_USERINFO%%:*}"
    DB_PASS="${DB_USERINFO#*:}"

    export SPRING_DATASOURCE_URL="jdbc:postgresql://${DB_HOSTPATH}?sslmode=require"
    export SPRING_DATASOURCE_USERNAME="$DB_USER"
    export SPRING_DATASOURCE_PASSWORD="$DB_PASS"
fi

exec java -jar app.jar
