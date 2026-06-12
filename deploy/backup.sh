#!/bin/bash
CONTAINER_NAME="arcabank_db"
DB_NAME="postgres"
DB_USER="postgres"

mkdir -p ./db_backups

FILENAME="./db_backups/snapshot_$(date +%Y-%m-%d_%H-%M-%S).sql"

echo "Створення бекапу бази $DB_NAME..."
docker exec -t $CONTAINER_NAME pg_dump -U $DB_USER $DB_NAME > $FILENAME

echo "Бекап успішно збережено: $FILENAME"
