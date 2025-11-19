# Healthcare Appointment System

A production-ready, HIPAA-Compliant Healthcare Appointment Management System built with Spring Boot 3.x and Java 17.

## Overview

This system demonstrates enterprise-level Spring Boot development with comprehensive features for managing healthcare appointments, medical records, video consultations, and more.

## Key Features

- **HIPAA Compliance**: AES-256 encryption for PHI data, comprehensive audit logging
- **OAuth2 Authentication**: Google OAuth2 + JWT-based authentication
- **Appointment Management**: Real-time availability, conflict detection, automated reminders
- **Video Consultations**: Twilio Video API integration for telemedicine
- **Medical Records**: Secure S3 storage with encryption
- **Prescription Management**: Digital prescription system
- **Multi-User Portal**: Separate interfaces for patients, doctors, and admins
- **Notifications**: Email and SMS notifications via Twilio

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Java Version**: 17+
- **Database**: PostgreSQL
- **Caching**: Redis
- **Security**: Spring Security OAuth2, JWT
- **Video**: Twilio Video API
- **Storage**: Amazon S3
- **Email/SMS**: Spring Mail, Twilio
- **Documentation**: Springdoc OpenAPI
- **Build Tool**: Maven

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- PostgreSQL 14+
- Redis 7+
- AWS Account (for S3)
- Twilio Account (for Video & SMS)
- Google OAuth2 Credentials

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd Healthcare-Appointment-System-
```

### 2. Configure Environment Variables

Create a `.env` file or set the following environment variables:

```properties
# Database
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password

# Security
JWT_SECRET=your_jwt_secret_key
ENCRYPTION_SECRET_KEY=your_encryption_key

# OAuth2
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# Twilio
TWILIO_ACCOUNT_SID=your_twilio_account_sid
TWILIO_API_KEY=your_twilio_api_key
TWILIO_API_SECRET=your_twilio_api_secret
TWILIO_PHONE_NUMBER=your_twilio_phone_number

# AWS S3
AWS_ACCESS_KEY_ID=your_aws_access_key
AWS_SECRET_ACCESS_KEY=your_aws_secret_key
AWS_S3_BUCKET_NAME=your_s3_bucket_name

# Email
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password

# Redis
REDIS_PASSWORD=your_redis_password
```

### 3. Set Up Database

```bash
# Create PostgreSQL database
createdb healthcare_db
```

### 4. Build the Project

```bash
mvn clean install
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Documentation

Once the application is running, access the API documentation at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API Docs: `http://localhost:8080/api-docs`

## Project Structure

```
com.healthcare/
├── entity/          # JPA entities
├── repository/      # Spring Data JPA repositories
├── dto/            # Data Transfer Objects
├── service/        # Business logic services
├── controller/     # REST controllers
├── security/       # Security configuration
├── config/         # Application configuration
├── util/           # Utility classes
├── exception/      # Custom exceptions
└── scheduler/      # Scheduled tasks
```

## Security Features

- **Data Encryption**: AES-256 encryption for all PHI data at rest
- **TLS/HTTPS**: All communications encrypted in transit
- **Audit Logging**: Comprehensive logging of all data access
- **Role-Based Access Control**: Fine-grained permissions
- **Session Management**: Secure session handling with Redis
- **OAuth2 Integration**: Secure authentication with Google

## Testing

```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## Docker Deployment

```bash
# Build and run with Docker Compose
docker-compose up -d
```

## Contributing

This is a portfolio project. For issues or suggestions, please open an issue.

## License

This project is for educational and portfolio purposes.

## Author

Built as a demonstration of enterprise-level Spring Boot development with healthcare domain expertise.

---

**Status**: 🚧 In Development

**Current Version**: 1.0.0-SNAPSHOT
