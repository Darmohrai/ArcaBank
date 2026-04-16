SELECT 'CREATE DATABASE arcabank_db'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'arcabank_db')\gexec
