# Healthcare Appointment System - Deployment Guide

## Prerequisites

- Docker & Docker Compose installed
- AWS Account (for S3)
- Twilio Account (for SMS & Video)
- Email SMTP credentials (Gmail, SendGrid, etc.)
- Domain name (for production)
- SSL certificates

## Quick Start (Development)

```bash
# 1. Clone the repository
git clone <repository-url>
cd Healthcare-Appointment-System

# 2. Copy environment file
cp .env.example .env

# 3. Update .env with your credentials
nano .env

# 4. Start services
docker-compose up -d

# 5. Check logs
docker-compose logs -f app

# 6. Access application
open http://localhost:8080
```

## Production Deployment

### 1. Environment Setup

```bash
# Generate secure keys
ENCRYPTION_KEY=$(openssl rand -base64 32)
JWT_SECRET=$(openssl rand -base64 64)
DB_PASSWORD=$(openssl rand -base64 24)
REDIS_PASSWORD=$(openssl rand -base64 24)

# Update .env file
cat > .env << EOF
DB_USER=healthcare
DB_PASSWORD=${DB_PASSWORD}
REDIS_PASSWORD=${REDIS_PASSWORD}

# AWS S3
AWS_ACCESS_KEY=your_aws_access_key
AWS_SECRET_KEY=your_aws_secret_key
AWS_REGION=us-east-1
S3_BUCKET_NAME=healthcare-records-prod

# Twilio
TWILIO_ACCOUNT_SID=your_account_sid
TWILIO_AUTH_TOKEN=your_auth_token
TWILIO_API_KEY=your_api_key
TWILIO_API_SECRET=your_api_secret
TWILIO_PHONE_NUMBER=+1234567890

# Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password

# Security
ENCRYPTION_SECRET_KEY=${ENCRYPTION_KEY}
JWT_SECRET=${JWT_SECRET}

# Application
APP_BASE_URL=https://yourdomain.com
EOF
```

### 2. SSL Certificate Setup

```bash
# Create SSL directory
mkdir -p ssl

# Option 1: Let's Encrypt (Recommended)
certbot certonly --standalone -d yourdomain.com
cp /etc/letsencrypt/live/yourdomain.com/fullchain.pem ssl/cert.pem
cp /etc/letsencrypt/live/yourdomain.com/privkey.pem ssl/key.pem

# Option 2: Self-signed (Development only)
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout ssl/key.pem -out ssl/cert.pem
```

### 3. AWS S3 Bucket Setup

```bash
# Create S3 bucket
aws s3 mb s3://healthcare-records-prod --region us-east-1

# Enable encryption
aws s3api put-bucket-encryption \
  --bucket healthcare-records-prod \
  --server-side-encryption-configuration \
  '{"Rules":[{"ApplyServerSideEncryptionByDefault":{"SSEAlgorithm":"AES256"}}]}'

# Set bucket policy (private)
aws s3api put-bucket-policy --bucket healthcare-records-prod --policy '{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Deny",
    "Principal": "*",
    "Action": "s3:*",
    "Resource": [
      "arn:aws:s3:::healthcare-records-prod/*",
      "arn:aws:s3:::healthcare-records-prod"
    ],
    "Condition": {
      "Bool": {"aws:SecureTransport": "false"}
    }
  }]
}'

# Enable versioning
aws s3api put-bucket-versioning \
  --bucket healthcare-records-prod \
  --versioning-configuration Status=Enabled
```

### 4. Database Initialization

```bash
# Start database only
docker-compose up -d postgres

# Wait for database to be ready
until docker-compose exec postgres pg_isready; do sleep 1; done

# Create initial admin user (optional)
docker-compose exec postgres psql -U healthcare -d healthcare -c "
INSERT INTO users (email, password, user_type, is_active, is_email_verified, created_at)
VALUES ('admin@healthcare.com', '\$2a\$10\$...', 'ADMIN', true, true, NOW());
"
```

### 5. Deploy Application

```bash
# Build and start all services
docker-compose up -d --build

# Verify all services are running
docker-compose ps

# Check application logs
docker-compose logs -f app

# Wait for application to be healthy
until curl -f http://localhost:8080/actuator/health; do sleep 5; done
```

### 6. Nginx Configuration

Update `nginx.conf` with your domain:

```nginx
server_name yourdomain.com www.yourdomain.com;
```

Restart nginx:

```bash
docker-compose restart nginx
```

### 7. Verify Deployment

```bash
# Test health endpoint
curl https://yourdomain.com/actuator/health

# Test API
curl https://yourdomain.com/api/auth/health

# Check logs
docker-compose logs --tail=100 app
docker-compose logs --tail=100 nginx
```

## Monitoring & Maintenance

### Health Checks

```bash
# Application health
curl http://localhost:8080/actuator/health

# Database health
docker-compose exec postgres pg_isready

# Redis health
docker-compose exec redis redis-cli ping
```

### Logs

```bash
# View logs
docker-compose logs -f app
docker-compose logs -f postgres
docker-compose logs -f redis
docker-compose logs -f nginx

# Export logs
docker-compose logs app > app.log
```

### Backups

```bash
# Database backup
docker-compose exec postgres pg_dump -U healthcare healthcare > backup_$(date +%Y%m%d).sql

# Restore from backup
docker-compose exec -T postgres psql -U healthcare healthcare < backup_20231120.sql

# S3 backup (automatic via S3 versioning)
aws s3 sync s3://healthcare-records-prod s3://healthcare-records-backup
```

### Updates

```bash
# Pull latest code
git pull origin main

# Rebuild and restart
docker-compose down
docker-compose up -d --build

# Or zero-downtime update
docker-compose up -d --build --no-deps app
```

## Security Checklist

- [ ] Strong database password (20+ characters)
- [ ] Strong Redis password
- [ ] Secure JWT secret (64+ characters)
- [ ] Secure encryption key (32+ bytes)
- [ ] HTTPS enabled with valid SSL certificate
- [ ] Firewall configured (ports 80, 443 only)
- [ ] Rate limiting enabled in nginx
- [ ] Security headers configured
- [ ] S3 bucket encryption enabled
- [ ] Database encryption at rest
- [ ] Regular security updates
- [ ] Audit logs enabled
- [ ] Backup strategy in place

## Troubleshooting

### Application won't start

```bash
# Check logs
docker-compose logs app

# Check environment variables
docker-compose config

# Verify database connection
docker-compose exec app ping postgres
```

### Database connection errors

```bash
# Check postgres status
docker-compose ps postgres

# Check database logs
docker-compose logs postgres

# Test connection
docker-compose exec postgres psql -U healthcare -d healthcare -c "SELECT 1;"
```

### Email not sending

```bash
# Check mail configuration
docker-compose exec app env | grep MAIL

# Test SMTP connection
telnet smtp.gmail.com 587
```

### S3 upload errors

```bash
# Verify AWS credentials
docker-compose exec app env | grep AWS

# Test S3 access
aws s3 ls s3://healthcare-records-prod --profile default
```

## Scaling

### Horizontal Scaling

```yaml
# docker-compose-scale.yml
services:
  app:
    deploy:
      replicas: 3

  nginx:
    # Add load balancing
    upstream healthcare_backend {
      least_conn;
      server app_1:8080;
      server app_2:8080;
      server app_3:8080;
    }
```

### Database Replication

```yaml
postgres-replica:
  image: postgres:15-alpine
  environment:
    POSTGRES_PASSWORD: ${DB_PASSWORD}
  command: >
    postgres
    -c wal_level=replica
    -c hot_standby=on
```

## Production Checklist

- [ ] All environment variables configured
- [ ] SSL certificates installed
- [ ] Database backups scheduled
- [ ] Monitoring configured
- [ ] Logging configured
- [ ] Rate limiting tested
- [ ] Security scan completed
- [ ] Load testing completed
- [ ] Disaster recovery plan documented
- [ ] HIPAA compliance verified
- [ ] BAA signed with third parties (Twilio, AWS)

## Support

For issues or questions:
- GitHub Issues: <repository-url>/issues
- Documentation: <repository-url>/wiki
- Email: support@healthcare.com
