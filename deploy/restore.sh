#!/bin/bash
CONTAINER_NAME="arcabank_db"
DB_USER="postgres"
DB_NAME="postgres"

if [ -z "$1" ]; then
    echo "Помилка: Вкажіть файл для відновлення. Використання: ./restore.sh ./db_backups/snapshot_YYYY-MM-DD_HH-MM-SS.sql"
    exit 1
fi

echo "Відновлення бази $DB_NAME з файлу $1..."
cat $1 | docker exec -i $CONTAINER_NAME psql -U $DB_USER -d $DB_NAME

echo "Дані успішно відновлено."
