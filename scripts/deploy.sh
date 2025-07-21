#!/bin/bash

# TwitterX Deployment Script
# This script pulls the latest Docker image and restarts the application

set -e

# Configuration
GITHUB_REPO="programistich/twitterx"
IMAGE_NAME="ghcr.io/${GITHUB_REPO}:latest"
COMPOSE_FILE="docker-compose.prod.yml"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] WARNING:${NC} $1"
}

error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ERROR:${NC} $1"
}

# Check if running as root
if [[ $EUID -eq 0 ]]; then
   warn "This script should not be run as root for security reasons"
fi

# Check if Docker is installed and running
if ! command -v docker &> /dev/null; then
    error "Docker is not installed"
    exit 1
fi

if ! docker info &> /dev/null; then
    error "Docker daemon is not running"
    exit 1
fi

# Check if Docker Compose is available
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    error "Docker Compose is not installed"
    exit 1
fi

# Function to use correct docker compose command
docker_compose() {
    if command -v docker-compose &> /dev/null; then
        docker-compose "$@"
    else
        docker compose "$@"
    fi
}

log "Starting deployment process..."

# Check if .env file exists
if [[ ! -f .env ]]; then
    warn ".env file not found. Make sure to create one with required environment variables."
    echo "Required variables: TELEGRAM_BOT_TOKEN, TELEGRAM_BOT_USERNAME, POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD"
fi

# Check if compose file exists
if [[ ! -f $COMPOSE_FILE ]]; then
    error "Docker Compose file '$COMPOSE_FILE' not found"
    exit 1
fi

# Create backup of current running containers
log "Creating backup information..."
docker_compose -f $COMPOSE_FILE ps > deployment_backup_$(date +%Y%m%d_%H%M%S).txt

# Pull the latest image
log "Pulling latest Docker image: $IMAGE_NAME"
if ! docker pull $IMAGE_NAME; then
    error "Failed to pull Docker image"
    exit 1
fi

# Stop the application (keep database running)
log "Stopping application service..."
docker_compose -f $COMPOSE_FILE stop twitter

# Start the updated application
log "Starting application with new image..."
if ! docker_compose -f $COMPOSE_FILE up -d twitter; then
    error "Failed to start application"
    log "Attempting to rollback..."
    docker_compose -f $COMPOSE_FILE restart twitter
    exit 1
fi

# Wait for application to be ready
log "Waiting for application to be ready..."
sleep 10

# Check if application is healthy
log "Checking application health..."
if docker_compose -f $COMPOSE_FILE ps twitter | grep -q "healthy\|Up"; then
    log "Application is running successfully"
else
    warn "Application health check failed. Check logs with: docker-compose -f $COMPOSE_FILE logs twitter"
fi

# Clean up old Docker images
log "Cleaning up old Docker images..."
docker image prune -f

# Show running services
log "Current running services:"
docker_compose -f $COMPOSE_FILE ps

log "Deployment completed successfully!"
log "You can check logs with: docker-compose -f $COMPOSE_FILE logs -f twitter"