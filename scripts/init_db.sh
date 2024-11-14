#!/usr/bin/env bash

# print each command before executing
set -x
# 1. Exit immediately if a command exits with a non-zero status
# 2. Exit immediately if a pipeline returns a non-zero status
set -eo pipefail

DB_USER=${POSTGRES_USER:=postgres}
DB_PASSWORD="${POSTGRES_PASSWORD:=password}"
DB_NAME="${POSTGRES_DB:=fruitlabs}"
DB_PORT="${POSTGRES_PORT:=5432}"

# Stop and remove any existing Docker container
CONTAINER_ID=$(docker ps -q --filter "ancestor=postgres" --filter "name=${DB_NAME}")
if [ -n "$CONTAINER_ID" ]; then
  docker stop "${CONTAINER_ID}"
  docker rm "${CONTAINER_ID}"
fi

docker run \
  --name "${DB_NAME}" \
  -e POSTGRES_USER="${DB_USER}" \
  -e POSTGRES_PASSWORD="${DB_PASSWORD}" \
  -e POSTGRES_DB="${DB_NAME}" \
  -p "${DB_PORT}":5432 \
  -d postgres \
  postgres -N 1000


# Wait for the database to be ready
timeout=10
until docker exec "${DB_NAME}" pg_isready > /dev/null 2>&1; do
  if (( timeout == 0 )); then
    echo "Error: Timeout when waiting for PostgreSQL to start"
    exit 1
  fi
  echo "Waiting for PostgreSQL to start..."
  sleep 1
  timeout=$((timeout - 1))
done
echo "PostgreSQL has started"

# Add the schema to the database
docker cp scripts/init_db.sql "${DB_NAME}":/init_db.sql
docker exec -i "${DB_NAME}" psql -U "${DB_USER}" -d "${DB_NAME}" -f init_db.sql

# Generate Fruits data
./scripts/fruitlabs_inventory_gen.sc

# Add the data to the database
docker cp scripts/fruits_inventory.csv "${DB_NAME}":/fruits_inventory.csv
docker exec -i "${DB_NAME}" psql -U "${DB_USER}" -d "${DB_NAME}" -c "COPY inventory(name, quantity, pricePerPound) FROM '/fruits_inventory.csv' DELIMITER ',' CSV HEADER;"
