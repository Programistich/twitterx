# TwitterX Deployment Guide

This guide explains how to set up CI/CD pipeline and deploy TwitterX to your server.

## Overview

The deployment process consists of:
1. **GitHub Actions CI/CD** - Automatically builds and pushes Docker images
2. **Docker Registry** - Stores built images (GitHub Container Registry)
3. **Server Deployment** - Pulls and runs the latest image on your server

## Prerequisites

### On your server:
- Docker and Docker Compose installed
- SSH access configured
- Git (optional, for cloning configuration files)

### GitHub Repository Secrets:
You need to configure the following secrets in your GitHub repository (`Settings` → `Secrets and variables` → `Actions`):

| Secret Name | Description | Example |
|-------------|-------------|---------|
| `SERVER_HOST` | Your server IP or domain | `192.168.1.100` or `yourserver.com` |
| `SERVER_USER` | SSH username | `ubuntu` or `deploy` |
| `SERVER_SSH_KEY` | Private SSH key for server access | Content of your `~/.ssh/id_rsa` |
| `SERVER_PORT` | SSH port (optional, defaults to 22) | `22` |
| `DEPLOY_PATH` | Path to your application on server | `/home/ubuntu/twitterx` |

## Setup Instructions

### 1. Server Setup

```bash
# 1. Clone or copy necessary files to your server
scp docker-compose.prod.yml user@yourserver:/path/to/app/
scp scripts/deploy.sh user@yourserver:/path/to/app/
scp scripts/nitter.conf user@yourserver:/path/to/app/
scp scripts/cookies.txt user@yourserver:/path/to/app/

# 2. Create environment file
cat > .env << 'EOF'
# Telegram Bot Configuration
TELEGRAM_BOT_TOKEN=your_bot_token
TELEGRAM_BOT_USERNAME=your_bot_username

# Database Configuration
POSTGRES_DB=twitterx
POSTGRES_USER=twitterx
POSTGRES_PASSWORD=your_secure_password

# AI Configuration (optional)
SECURE_1PSID=your_google_cookie
SECURE_1PSIDTS=your_google_cookie
OPEN_ROUTER_API_KEY=your_openrouter_key
EOF

# 3. Make deploy script executable
chmod +x deploy.sh

# 4. Initial deployment
./deploy.sh
```

### 2. GitHub Actions Setup

The CI/CD pipeline is automatically configured in `.github/workflows/deploy.yml`. It will:

1. **Test** - Run tests and linting on every push/PR
2. **Build** - Build and push Docker image to GitHub Container Registry (on main branch)
3. **Deploy** - SSH to your server and deploy the new image (on main branch)

#### Workflow Triggers:
- **Pull Requests** - Runs tests only
- **Push to main** - Runs tests, builds image, and deploys

### 3. Manual Deployment

If you need to deploy manually or want to test the deployment:

```bash
# On your server
cd /path/to/your/app
./deploy.sh
```

## File Structure

```
your-server/
├── docker-compose.prod.yml    # Production Docker Compose configuration
├── .env                       # Environment variables (create this)
├── deploy.sh                  # Deployment script
├── nitter.conf               # Nitter configuration
├── cookies.txt               # yt-dlp cookies
└── logs/                     # Application logs (auto-created)
```

## Environment Variables

### Required Variables:
- `TELEGRAM_BOT_TOKEN` - Get from @BotFather
- `TELEGRAM_BOT_USERNAME` - Your bot username without @
- `POSTGRES_DB` - Database name
- `POSTGRES_USER` - Database username  
- `POSTGRES_PASSWORD` - Database password

### Optional Variables:
- `SECURE_1PSID` - Google cookie for AI features
- `SECURE_1PSIDTS` - Google cookie for AI features
- `OPEN_ROUTER_API_KEY` - OpenRouter API key for AI

## Monitoring and Logs

### View logs:
```bash
# All services
docker-compose -f docker-compose.prod.yml logs -f

# Specific service
docker-compose -f docker-compose.prod.yml logs -f twitter

# Last 100 lines
docker-compose -f docker-compose.prod.yml logs --tail=100 twitter
```

### Check service status:
```bash
docker-compose -f docker-compose.prod.yml ps
```

### Health checks:
```bash
# Application health
curl http://localhost:8080/actuator/health

# Database health
docker-compose -f docker-compose.prod.yml exec twitter-db pg_isready

# Nitter health
curl http://localhost:8049/elonmusk
```

## Troubleshooting

### Common Issues:

1. **Image pull fails**:
   ```bash
   # Check if you have access to the GitHub Container Registry
   docker login ghcr.io -u your-github-username
   ```

2. **Database connection fails**:
   ```bash
   # Check database logs
   docker-compose -f docker-compose.prod.yml logs twitter-db
   ```

3. **Application fails to start**:
   ```bash
   # Check application logs
   docker-compose -f docker-compose.prod.yml logs twitter
   
   # Check environment variables
   docker-compose -f docker-compose.prod.yml exec twitter env | grep -E "(TELEGRAM|POSTGRES)"
   ```

4. **SSH deployment fails**:
   - Check SSH key is correct and has no passphrase
   - Verify server host, user, and port in GitHub secrets
   - Ensure deploy path exists and user has permissions

### Recovery:

If deployment fails, you can rollback:

```bash
# List available images
docker images ghcr.io/programistich/twitterx

# Run specific version
docker-compose -f docker-compose.prod.yml down twitter
docker run -d --name twitter-rollback ghcr.io/programistich/twitterx:previous-tag
```

## Security Considerations

1. **SSH Key**: Use a dedicated SSH key for deployment, not your personal key
2. **Environment Variables**: Never commit `.env` files to version control
3. **Firewall**: Only expose necessary ports (8080 for app, 5434 for db if needed externally)
4. **Updates**: Regularly update base Docker images and dependencies

## Scaling

For high-traffic scenarios:

1. **Multiple app instances**:
   ```yaml
   twitter:
     deploy:
       replicas: 3
   ```

2. **External database**: Use managed PostgreSQL service
3. **Load balancer**: Add nginx or similar in front of the application
4. **Monitoring**: Add Prometheus + Grafana for metrics

## Backup

### Database backup:
```bash
# Create backup
docker-compose -f docker-compose.prod.yml exec twitter-db pg_dump -U $POSTGRES_USER $POSTGRES_DB > backup_$(date +%Y%m%d).sql

# Restore backup
docker-compose -f docker-compose.prod.yml exec -T twitter-db psql -U $POSTGRES_USER $POSTGRES_DB < backup_file.sql
```

### Full backup:
```bash
# Backup volumes
docker run --rm -v twitterx_postgres-data:/data -v $(pwd):/backup alpine tar czf /backup/postgres-backup.tar.gz -C /data .
```