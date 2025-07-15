#!/bin/bash
set -e

if [ -f ".env" ]; then
    echo "Loading .env..."
    source .env
fi

# Down previous Docker containers
echo "Stopping previous Docker containers..."
docker compose down --remove-orphans

# Run Docker containers
echo "Building and starting Docker containers..."
docker compose build
docker compose up -d
echo "Docker containers are running!"

echo "You can check the logs with: docker compose logs -f"
