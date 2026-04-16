#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "postgres" <<-EOSQL
    CREATE DATABASE arcabank_core;
    CREATE DATABASE arcabank_auth;
    CREATE DATABASE arcabank_notif;
    CREATE DATABASE arcabank_keycloak;
EOSQL
