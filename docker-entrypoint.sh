#!/bin/sh
set -e

# Convert Railway/Docker DATABASE_URL to JDBC format.
# Railway provides: postgres://user:pass@host:port/db  (or postgresql://)
if [ -n "$DATABASE_URL" ]; then
    # Strip both "postgres://" and "postgresql://" schemes
    DB_WITHOUT_SCHEME="${DATABASE_URL#postgres://}"
    DB_WITHOUT_SCHEME="${DB_WITHOUT_SCHEME#ql://}"   # handles if first strip left "ql://"

    # Re-do cleanly: remove any leading scheme
    CLEAN="${DATABASE_URL}"
    case "$CLEAN" in
        postgresql://*) CLEAN="${CLEAN#postgresql://}" ;;
        postgres://*)   CLEAN="${CLEAN#postgres://}" ;;
    esac

    DB_USER="${CLEAN%%:*}"
    AFTER_USER="${CLEAN#*:}"
    DB_PASS="${AFTER_USER%%@*}"
    DB_HOSTPATH="${AFTER_USER#*@}"

    export SPRING_DATASOURCE_URL="jdbc:postgresql://${DB_HOSTPATH}?sslmode=require"
    export SPRING_DATASOURCE_USERNAME="$DB_USER"
    export SPRING_DATASOURCE_PASSWORD="$DB_PASS"

    echo "[entrypoint] DB_URL  => $SPRING_DATASOURCE_URL"
    echo "[entrypoint] DB_USER => $SPRING_DATASOURCE_USERNAME"
fi

exec java -jar app.jar
