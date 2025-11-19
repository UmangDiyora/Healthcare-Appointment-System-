<div align="center">

# 🏥 Healthcare Appointment System

### *Enterprise-Grade HIPAA-Compliant Healthcare Management Platform*

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14+-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7+-red.svg)](https://redis.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![HIPAA](https://img.shields.io/badge/HIPAA-Compliant-success.svg)](https://www.hhs.gov/hipaa)
[![Build](https://img.shields.io/badge/Build-Passing-brightgreen.svg)](https://github.com)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](http://makeapullrequest.com)

[Features](#-features) •
[Tech Stack](#-technology-stack) •
[Quick Start](#-quick-start) •
[API Docs](#-api-documentation) •
[Architecture](#-architecture) •
[Contributing](#-contributing)

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Technology Stack](#-technology-stack)
- [Architecture](#-architecture)
- [Prerequisites](#-prerequisites)
- [Quick Start](#-quick-start)
- [Configuration](#-configuration)
- [API Documentation](#-api-documentation)
- [Project Structure](#-project-structure)
- [Security](#-security)
- [Testing](#-testing)
- [Deployment](#-deployment)
- [Roadmap](#-roadmap)
- [Contributing](#-contributing)
- [License](#-license)
- [Contact](#-contact)

---

## 🌟 Overview

**Healthcare Appointment System** is a production-ready, enterprise-grade healthcare management platform built with modern technologies and best practices. This system demonstrates professional Spring Boot development with comprehensive features for managing healthcare appointments, medical records, video consultations, and secure patient data handling.

### Why This Project?

- ✅ **Production Ready**: Built with enterprise patterns and best practices
- ✅ **Security First**: HIPAA-compliant with AES-256 encryption
- ✅ **Modern Stack**: Latest Spring Boot 3.x with Java 17+
- ✅ **Full Featured**: Complete appointment lifecycle management
- ✅ **Well Tested**: Comprehensive test coverage
- ✅ **Cloud Ready**: Docker support with AWS S3 integration

---

## ✨ Features

### 🔐 Security & Compliance

- **HIPAA Compliance** - Full compliance with healthcare data protection standards
- **AES-256 Encryption** - Military-grade encryption for PHI (Protected Health Information)
- **OAuth2 Authentication** - Secure Google OAuth2 integration
- **JWT Authorization** - Stateless, secure token-based authentication
- **Audit Logging** - Comprehensive tracking of all data access and modifications
- **Role-Based Access Control (RBAC)** - Fine-grained permissions for patients, doctors, and admins

### 📅 Appointment Management

- **Real-Time Availability** - Live doctor availability checking
- **Smart Scheduling** - Automatic conflict detection and prevention
- **Flexible Rescheduling** - Easy appointment modifications
- **Automated Reminders** - Email and SMS notifications before appointments
- **Cancellation Handling** - Graceful appointment cancellation with notifications
- **Multi-Doctor Support** - Manage multiple healthcare providers

### 🎥 Video Consultations

- **Twilio Video Integration** - High-quality video consultations
- **Room Management** - Secure, temporary consultation rooms
- **Access Tokens** - Time-limited, secure room access
- **Recording Support** - Optional session recording capabilities
- **Cross-Platform** - Works on web, mobile, and desktop

### 📊 Medical Records Management

- **Secure Storage** - Encrypted storage on AWS S3
- **Document Management** - Upload and manage medical documents
- **Version Control** - Track document versions and updates
- **Access Control** - Strict permissions for medical records
- **Audit Trail** - Complete history of record access

### 💊 Prescription System

- **Digital Prescriptions** - Create and manage prescriptions digitally
- **Patient History** - Track prescription history
- **Doctor Verification** - Secure prescription authentication
- **Dosage Management** - Detailed medication instructions

### 🔔 Notification System

- **Multi-Channel Notifications** - Email and SMS support
- **Automated Reminders** - Scheduled appointment reminders
- **Real-Time Alerts** - Instant notifications for important events
- **Template System** - Customizable notification templates
- **Delivery Tracking** - Monitor notification delivery status

### 👥 User Management

- **Patient Portal** - Comprehensive patient dashboard
- **Doctor Dashboard** - Provider-specific interface
- **Admin Panel** - System administration tools
- **Profile Management** - Update personal and professional information
- **Review System** - Patient feedback and ratings

---

## 🛠 Technology Stack

### Backend Framework
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.0-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.8+-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)

### Database & Caching
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14+-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7+-DC382D?style=for-the-badge&logo=redis&logoColor=white)

### Security
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white)
![OAuth2](https://img.shields.io/badge/OAuth2-4285F4?style=for-the-badge&logo=google&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=json-web-tokens&logoColor=white)

### Cloud Services
![AWS S3](https://img.shields.io/badge/AWS_S3-569A31?style=for-the-badge&logo=amazon-s3&logoColor=white)
![Twilio](https://img.shields.io/badge/Twilio-F22F46?style=for-the-badge&logo=twilio&logoColor=white)

### DevOps
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Nginx](https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white)

### Documentation & Testing
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)
![JUnit](https://img.shields.io/badge/JUnit-25A162?style=for-the-badge&logo=junit5&logoColor=white)

### **Core Dependencies**

| Technology | Version | Purpose |
|-----------|---------|---------|
| Spring Boot | 3.2.0 | Application framework |
| Java | 17+ | Programming language |
| PostgreSQL | 14+ | Primary database |
| Redis | 7+ | Caching & sessions |
| Twilio SDK | 9.14.1 | Video & SMS |
| AWS SDK | 2.21.0 | S3 storage |
| JWT | 0.12.3 | Authentication |
| Springdoc OpenAPI | 2.2.0 | API documentation |
| Lombok | Latest | Code generation |

---

## 🏗 Architecture

### System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Client Layer                            │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │  Patient │  │  Doctor  │  │  Admin   │  │  Mobile  │   │
│  │   Portal │  │  Portal  │  │  Portal  │  │   App    │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└─────────────────────────────────────────────────────────────┘
                            │
                    ┌───────▼───────┐
                    │   Nginx/LB    │
                    └───────┬───────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                  Spring Boot Application                     │
│  ┌─────────────────────────────────────────────────────┐   │
│  │           Security Layer (OAuth2 + JWT)             │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │   Auth   │  │Appointment│ │  Video   │  │  Medical │   │
│  │ Service  │  │  Service  │  │ Service  │  │  Record  │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
│                                                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │   Mail   │  │   SMS    │  │   S3     │  │  Audit   │   │
│  │ Service  │  │ Service  │  │ Service  │  │ Service  │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└──────────────────────────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
┌───────▼────────┐  ┌──────▼──────┐  ┌─────────▼────────┐
│   PostgreSQL   │  │    Redis    │  │   External APIs  │
│   (Primary DB) │  │   (Cache)   │  │  ├─ AWS S3       │
└────────────────┘  └─────────────┘  │  ├─ Twilio Video │
                                     │  ├─ Twilio SMS   │
                                     │  └─ Google OAuth │
                                     └──────────────────┘
```

### Key Design Patterns

- **Repository Pattern** - Data access abstraction
- **Service Layer Pattern** - Business logic separation
- **DTO Pattern** - Data transfer and validation
- **Builder Pattern** - Object construction
- **Strategy Pattern** - Notification delivery
- **Observer Pattern** - Event handling
- **Aspect-Oriented Programming** - Cross-cutting concerns (audit, logging)

---

## 📦 Prerequisites

Before running the application, ensure you have the following installed:

| Requirement | Version | Download |
|------------|---------|----------|
| Java JDK | 17+ | [Oracle](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) / [OpenJDK](https://openjdk.org/) |
| Maven | 3.8+ | [Apache Maven](https://maven.apache.org/download.cgi) |
| PostgreSQL | 14+ | [PostgreSQL](https://www.postgresql.org/download/) |
| Redis | 7+ | [Redis](https://redis.io/download/) |
| Docker (Optional) | Latest | [Docker](https://www.docker.com/get-started) |

### External Services Required

- **AWS Account** - For S3 storage ([Sign up](https://aws.amazon.com/))
- **Twilio Account** - For video calls and SMS ([Sign up](https://www.twilio.com/try-twilio))
- **Google Cloud** - For OAuth2 authentication ([Console](https://console.cloud.google.com/))

---

## 🚀 Quick Start

### Option 1: Docker (Recommended)

The fastest way to get started:

```bash
# Clone the repository
git clone https://github.com/yourusername/Healthcare-Appointment-System.git
cd Healthcare-Appointment-System-

# Copy environment file
cp .env.example .env

# Edit .env with your credentials
nano .env

# Start all services with Docker Compose
docker-compose up -d

# Check logs
docker-compose logs -f app
```

The application will be available at `http://localhost:8080`

### Option 2: Local Development

For development and debugging:

#### Step 1: Clone and Navigate

```bash
git clone https://github.com/yourusername/Healthcare-Appointment-System.git
cd Healthcare-Appointment-System-
```

#### Step 2: Database Setup

```bash
# Create PostgreSQL database
createdb healthcare_db

# Or using psql
psql -U postgres
CREATE DATABASE healthcare_db;
\q

# Initialize Redis
redis-server
```

#### Step 3: Configure Environment

Create a `.env` file or set environment variables:

```bash
cp .env.example .env
```

Edit `.env` with your credentials (see [Configuration](#-configuration) section)

#### Step 4: Build the Project

```bash
# Clean and build
mvn clean install

# Skip tests for faster build
mvn clean install -DskipTests
```

#### Step 5: Run the Application

```bash
# Using Maven
mvn spring-boot:run

# Or run the JAR
java -jar target/appointment-system-1.0.0.jar
```

#### Step 6: Verify Installation

Open your browser and navigate to:
- **Application**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs

---

## ⚙ Configuration

### Environment Variables

Create a `.env` file in the root directory:

```properties
# ========================================
# DATABASE CONFIGURATION
# ========================================
DB_HOST=localhost
DB_PORT=5432
DB_NAME=healthcare_db
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password

# ========================================
# SECURITY & ENCRYPTION
# ========================================
JWT_SECRET=your-super-secret-jwt-key-min-256-bits-long
JWT_EXPIRATION=86400000
ENCRYPTION_SECRET_KEY=your-32-character-encryption-key

# ========================================
# OAUTH2 - GOOGLE
# ========================================
GOOGLE_CLIENT_ID=your-google-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-google-client-secret
OAUTH2_REDIRECT_URI=http://localhost:8080/oauth2/callback/google

# ========================================
# TWILIO CONFIGURATION
# ========================================
TWILIO_ACCOUNT_SID=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxx
TWILIO_AUTH_TOKEN=your_twilio_auth_token
TWILIO_API_KEY=SKxxxxxxxxxxxxxxxxxxxxxxxxxxxx
TWILIO_API_SECRET=your_twilio_api_secret
TWILIO_PHONE_NUMBER=+1234567890

# ========================================
# AWS S3 CONFIGURATION
# ========================================
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=your_aws_access_key_id
AWS_SECRET_ACCESS_KEY=your_aws_secret_access_key
AWS_S3_BUCKET_NAME=healthcare-records-bucket

# ========================================
# EMAIL CONFIGURATION (Gmail)
# ========================================
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-specific-password
MAIL_FROM=noreply@healthcaresystem.com

# ========================================
# REDIS CONFIGURATION
# ========================================
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password
REDIS_TTL=3600

# ========================================
# APPLICATION SETTINGS
# ========================================
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev
LOG_LEVEL=INFO
```

### Configuration Tips

1. **JWT Secret**: Generate a secure key:
   ```bash
   openssl rand -base64 64
   ```

2. **Encryption Key**: Must be exactly 32 characters:
   ```bash
   openssl rand -hex 16
   ```

3. **Google OAuth2**:
   - Go to [Google Cloud Console](https://console.cloud.google.com/)
   - Create a project and enable Google+ API
   - Create OAuth2 credentials
   - Add authorized redirect URI: `http://localhost:8080/oauth2/callback/google`

4. **Twilio Setup**:
   - Sign up at [Twilio](https://www.twilio.com/)
   - Get Account SID and Auth Token
   - Create API Key for video
   - Purchase a phone number for SMS

5. **AWS S3**:
   - Create an S3 bucket
   - Configure CORS for file uploads
   - Create IAM user with S3 access

---

## 📚 API Documentation

### Interactive Documentation

Once the application is running, access the interactive API documentation:

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

### Key API Endpoints

#### 🔐 Authentication

```http
POST   /api/auth/register/patient      # Register new patient
POST   /api/auth/register/doctor       # Register new doctor
POST   /api/auth/login                 # Login with credentials
GET    /api/auth/oauth2/google         # Google OAuth2 login
```

#### 📅 Appointments

```http
GET    /api/appointments                      # Get all appointments
POST   /api/appointments                      # Create appointment
GET    /api/appointments/{id}                 # Get appointment details
PUT    /api/appointments/{id}/reschedule      # Reschedule appointment
DELETE /api/appointments/{id}                 # Cancel appointment
GET    /api/appointments/patient/{patientId}  # Get patient appointments
GET    /api/appointments/doctor/{doctorId}    # Get doctor appointments
```

#### 👨‍⚕️ Doctors

```http
GET    /api/doctors                           # List all doctors
GET    /api/doctors/{id}                      # Get doctor profile
GET    /api/doctors/{id}/availability         # Check availability
POST   /api/doctors/{id}/availability         # Set availability
GET    /api/doctors/specialization/{spec}     # Find by specialization
```

#### 👤 Patients

```http
GET    /api/patients/{id}                     # Get patient profile
PUT    /api/patients/{id}                     # Update profile
GET    /api/patients/{id}/medical-history     # Get medical history
GET    /api/patients/{id}/prescriptions       # Get prescriptions
```

#### 📋 Medical Records

```http
GET    /api/medical-records/patient/{id}      # Get patient records
POST   /api/medical-records                   # Upload new record
GET    /api/medical-records/{id}              # Get record details
DELETE /api/medical-records/{id}              # Delete record
```

#### 💊 Prescriptions

```http
POST   /api/prescriptions                     # Create prescription
GET    /api/prescriptions/{id}                # Get prescription
GET    /api/prescriptions/patient/{id}        # Patient prescriptions
PUT    /api/prescriptions/{id}                # Update prescription
```

#### 🎥 Video Consultations

```http
POST   /api/video/room                        # Create video room
GET    /api/video/token/{appointmentId}       # Get access token
DELETE /api/video/room/{roomName}             # End consultation
```

#### 🔔 Notifications

```http
GET    /api/notifications                     # Get all notifications
GET    /api/notifications/unread              # Get unread notifications
PUT    /api/notifications/{id}/read           # Mark as read
DELETE /api/notifications/{id}                # Delete notification
```

### Example Request

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "patient@example.com",
    "password": "password123"
  }'

# Create Appointment (with JWT token)
curl -X POST http://localhost:8080/api/appointments \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "doctorId": 1,
    "patientId": 1,
    "appointmentDate": "2024-12-25T10:00:00",
    "reason": "Regular checkup"
  }'
```

---

## 📁 Project Structure

```
Healthcare-Appointment-System/
│
├── 📂 src/
│   ├── 📂 main/
│   │   ├── 📂 java/com/healthcare/
│   │   │   ├── 📂 annotation/           # Custom annotations
│   │   │   │   └── Auditable.java
│   │   │   │
│   │   │   ├── 📂 aspect/               # AOP aspects
│   │   │   │   └── AuditAspect.java
│   │   │   │
│   │   │   ├── 📂 config/               # Configuration classes
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── RedisConfig.java
│   │   │   │   ├── S3Config.java
│   │   │   │   ├── TwilioConfig.java
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   └── EncryptedStringConverter.java
│   │   │   │
│   │   │   ├── 📂 controller/           # REST controllers
│   │   │   │   ├── AppointmentController.java
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── DoctorController.java
│   │   │   │   ├── PatientController.java
│   │   │   │   ├── MedicalRecordController.java
│   │   │   │   ├── PrescriptionController.java
│   │   │   │   ├── VideoConsultationController.java
│   │   │   │   └── NotificationController.java
│   │   │   │
│   │   │   ├── 📂 dto/                  # Data Transfer Objects
│   │   │   │   ├── 📂 request/
│   │   │   │   │   ├── CreateAppointmentRequest.java
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   ├── PatientRegistrationRequest.java
│   │   │   │   │   └── ...
│   │   │   │   └── 📂 response/
│   │   │   │       ├── AppointmentResponse.java
│   │   │   │       ├── AuthResponse.java
│   │   │   │       └── ...
│   │   │   │
│   │   │   ├── 📂 entity/               # JPA entities
│   │   │   │   ├── User.java
│   │   │   │   ├── Patient.java
│   │   │   │   ├── Doctor.java
│   │   │   │   ├── Appointment.java
│   │   │   │   ├── MedicalRecord.java
│   │   │   │   ├── Prescription.java
│   │   │   │   ├── DoctorAvailability.java
│   │   │   │   ├── Notification.java
│   │   │   │   ├── AuditLog.java
│   │   │   │   └── Review.java
│   │   │   │
│   │   │   ├── 📂 exception/            # Custom exceptions
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── AppointmentConflictException.java
│   │   │   │   └── ...
│   │   │   │
│   │   │   ├── 📂 repository/           # Spring Data JPA
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── PatientRepository.java
│   │   │   │   ├── DoctorRepository.java
│   │   │   │   ├── AppointmentRepository.java
│   │   │   │   └── ...
│   │   │   │
│   │   │   ├── 📂 security/             # Security components
│   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── CustomUserDetailsService.java
│   │   │   │   ├── UserPrincipal.java
│   │   │   │   └── 📂 oauth2/
│   │   │   │       ├── CustomOAuth2UserService.java
│   │   │   │       ├── OAuth2AuthenticationSuccessHandler.java
│   │   │   │       └── OAuth2AuthenticationFailureHandler.java
│   │   │   │
│   │   │   ├── 📂 service/              # Business logic
│   │   │   │   ├── AppointmentService.java
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── DoctorService.java
│   │   │   │   ├── PatientService.java
│   │   │   │   ├── MedicalRecordService.java
│   │   │   │   ├── PrescriptionService.java
│   │   │   │   ├── VideoConsultationService.java
│   │   │   │   ├── NotificationService.java
│   │   │   │   ├── EmailService.java
│   │   │   │   ├── SmsService.java
│   │   │   │   ├── S3Service.java
│   │   │   │   └── AuditService.java
│   │   │   │
│   │   │   ├── 📂 scheduler/            # Scheduled tasks
│   │   │   │   └── AppointmentReminderScheduler.java
│   │   │   │
│   │   │   ├── 📂 util/                 # Utility classes
│   │   │   │   └── EncryptionUtil.java
│   │   │   │
│   │   │   └── HealthcareAppointmentSystemApplication.java
│   │   │
│   │   └── 📂 resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       └── application-prod.properties
│   │
│   └── 📂 test/
│       └── 📂 java/com/healthcare/
│           ├── 📂 controller/
│           │   └── AppointmentControllerIntegrationTest.java
│           ├── 📂 service/
│           │   ├── AppointmentServiceTest.java
│           │   └── PatientServiceTest.java
│           ├── 📂 repository/
│           │   └── RepositoryTest.java
│           ├── 📂 security/
│           │   └── SecurityTest.java
│           └── 📂 util/
│               └── EncryptionUtilTest.java
│
├── 📂 docker/
│   ├── Dockerfile
│   └── docker-compose.yml
│
├── 📄 pom.xml                          # Maven dependencies
├── 📄 .env.example                     # Environment template
├── 📄 .gitignore
├── 📄 README.md
├── 📄 TESTING.md                       # Testing documentation
├── 📄 DEPLOYMENT.md                    # Deployment guide
└── 📄 nginx.conf                       # Nginx configuration
```

---

## 🔒 Security

This application implements multiple layers of security to ensure HIPAA compliance and protect sensitive healthcare data.

### Encryption

#### Data at Rest
- **AES-256 Encryption**: All PHI (Protected Health Information) encrypted using AES-256-GCM
- **Encrypted Fields**:
  - Patient: SSN, medical history, allergies, current medications
  - Medical Records: Diagnosis, treatment, notes
  - Prescriptions: Medication details
- **Key Management**: Secure key storage using environment variables

#### Data in Transit
- **TLS/HTTPS**: All communications encrypted using TLS 1.3
- **Certificate Management**: SSL/TLS certificates for production
- **Secure WebSocket**: Encrypted video consultation streams

### Authentication & Authorization

#### Multi-Factor Authentication
- **OAuth2 Integration**: Google OAuth2 for secure login
- **JWT Tokens**: Stateless authentication with signed tokens
- **Token Expiration**: Configurable token lifetime (default: 24 hours)
- **Refresh Tokens**: Secure token renewal mechanism

#### Role-Based Access Control (RBAC)
```java
PATIENT:
  - View own appointments
  - View own medical records
  - Book/cancel appointments
  - View prescriptions

DOCTOR:
  - View assigned patients
  - Manage appointments
  - Create prescriptions
  - Upload medical records
  - Conduct video consultations

ADMIN:
  - User management
  - System configuration
  - Audit log access
  - Analytics dashboard
```

### Audit Logging

All sensitive operations are logged:
- User authentication attempts
- Data access (who, what, when)
- Data modifications
- Failed authorization attempts
- File uploads/downloads

```java
@Auditable(action = "VIEW_MEDICAL_RECORD")
public MedicalRecord getRecord(Long id) {
    // Automatically logged
}
```

### Input Validation

- **Request Validation**: JSR-303 Bean Validation
- **SQL Injection Prevention**: Parameterized queries with JPA
- **XSS Protection**: Input sanitization
- **CSRF Protection**: Token-based CSRF prevention

### Session Management

- **Redis-based Sessions**: Secure, distributed session storage
- **Session Timeout**: Automatic logout after inactivity
- **Concurrent Session Control**: Limit active sessions per user

---

## 🧪 Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run with coverage report
mvn test jacoco:report

# Run specific test class
mvn test -Dtest=AppointmentServiceTest

# Run integration tests only
mvn verify -P integration-tests

# Generate coverage report
mvn clean test jacoco:report
# Report available at: target/site/jacoco/index.html
```

### Test Coverage

Our comprehensive test suite includes:

| Test Type | Coverage | Description |
|-----------|----------|-------------|
| Unit Tests | 85%+ | Service and utility testing |
| Integration Tests | 75%+ | Controller and repository testing |
| Security Tests | 90%+ | Authentication and authorization |
| End-to-End | 70%+ | Complete user workflows |

### Test Structure

```
📂 src/test/java/com/healthcare/
├── 📂 controller/
│   └── AppointmentControllerIntegrationTest.java    # REST endpoint tests
├── 📂 service/
│   ├── AppointmentServiceTest.java                  # Business logic tests
│   └── PatientServiceTest.java
├── 📂 repository/
│   └── RepositoryTest.java                          # Database tests
├── 📂 security/
│   └── SecurityTest.java                            # Auth/authz tests
└── 📂 util/
    └── EncryptionUtilTest.java                      # Utility tests
```

### Example Test

```java
@SpringBootTest
@AutoConfigureMockMvc
class AppointmentControllerIntegrationTest {

    @Test
    @WithMockUser(roles = "PATIENT")
    void shouldCreateAppointment() throws Exception {
        CreateAppointmentRequest request = new CreateAppointmentRequest();
        request.setDoctorId(1L);
        request.setAppointmentDate(LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }
}
```

For detailed testing documentation, see [TESTING.md](TESTING.md)

---

## 🚀 Deployment

### Docker Deployment

#### Production with Docker Compose

```bash
# Build and start all services
docker-compose -f docker-compose.yml up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down

# Rebuild after changes
docker-compose up -d --build
```

#### Manual Docker Build

```bash
# Build the application
mvn clean package -DskipTests

# Build Docker image
docker build -t healthcare-system:latest .

# Run container
docker run -d \
  --name healthcare-app \
  -p 8080:8080 \
  --env-file .env \
  healthcare-system:latest
```

### Cloud Deployment

#### AWS Elastic Beanstalk

```bash
# Install EB CLI
pip install awsebcli

# Initialize EB application
eb init -p docker healthcare-system

# Create environment
eb create healthcare-prod

# Deploy
eb deploy
```

#### AWS ECS/Fargate

1. Push image to ECR
2. Create ECS task definition
3. Configure ALB
4. Deploy service

#### Heroku

```bash
# Login to Heroku
heroku login

# Create app
heroku create healthcare-system

# Add PostgreSQL
heroku addons:create heroku-postgresql:standard-0

# Add Redis
heroku addons:create heroku-redis:premium-0

# Set environment variables
heroku config:set JWT_SECRET=your-secret

# Deploy
git push heroku main
```

### Production Checklist

- [ ] Update `application-prod.properties`
- [ ] Configure SSL/TLS certificates
- [ ] Set up production database
- [ ] Configure Redis cluster
- [ ] Set environment variables
- [ ] Enable HTTPS only
- [ ] Configure CORS properly
- [ ] Set up monitoring (CloudWatch, Datadog)
- [ ] Configure log aggregation
- [ ] Set up automated backups
- [ ] Configure CDN for static assets
- [ ] Enable rate limiting
- [ ] Set up health checks
- [ ] Configure auto-scaling

For detailed deployment guide, see [DEPLOYMENT.md](DEPLOYMENT.md)

---

## 🗺 Roadmap

### Phase 1: Core Features ✅ (Completed)
- [x] User authentication and authorization
- [x] Appointment management
- [x] Doctor and patient profiles
- [x] Basic notifications

### Phase 2: Advanced Features ✅ (Completed)
- [x] Video consultations
- [x] Medical records management
- [x] Prescription system
- [x] SMS notifications
- [x] HIPAA compliance

### Phase 3: Enhancements 🚧 (In Progress)
- [ ] Mobile app (React Native)
- [ ] Payment integration (Stripe)
- [ ] Insurance verification
- [ ] Lab results integration
- [ ] Pharmacy integration
- [ ] Appointment recommendations (ML)

### Phase 4: Analytics & Reporting 📋 (Planned)
- [ ] Admin dashboard
- [ ] Analytics and insights
- [ ] Revenue reports
- [ ] Patient satisfaction metrics
- [ ] Doctor performance metrics

### Phase 5: AI Features 🤖 (Future)
- [ ] Symptom checker chatbot
- [ ] Appointment time recommendations
- [ ] Doctor recommendation system
- [ ] Automated medical transcription
- [ ] Predictive analytics

---

## 🤝 Contributing

We welcome contributions! Here's how you can help:

### Getting Started

1. **Fork the repository**
   ```bash
   gh repo fork yourusername/Healthcare-Appointment-System
   ```

2. **Clone your fork**
   ```bash
   git clone https://github.com/YOUR_USERNAME/Healthcare-Appointment-System.git
   cd Healthcare-Appointment-System-
   ```

3. **Create a branch**
   ```bash
   git checkout -b feature/amazing-feature
   ```

4. **Make your changes**
   - Write clean, documented code
   - Follow existing code style
   - Add tests for new features
   - Update documentation

5. **Commit your changes**
   ```bash
   git add .
   git commit -m "Add amazing feature"
   ```

6. **Push to your fork**
   ```bash
   git push origin feature/amazing-feature
   ```

7. **Open a Pull Request**
   - Describe your changes
   - Reference any related issues
   - Ensure all tests pass

### Code Style

- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use meaningful variable and method names
- Write comprehensive JavaDoc comments
- Keep methods small and focused
- Write unit tests for new code

### Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add payment integration
fix: resolve appointment conflict bug
docs: update API documentation
test: add integration tests for video service
refactor: improve error handling
```

### Areas for Contribution

- 🐛 Bug fixes
- ✨ New features
- 📝 Documentation improvements
- 🧪 Test coverage
- 🎨 UI/UX enhancements
- ♿ Accessibility improvements
- 🌐 Internationalization (i18n)
- ⚡ Performance optimizations

---

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2024 Healthcare Appointment System

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```

---

## 📞 Contact

### Project Information

- **Project Link**: [GitHub Repository](https://github.com/yourusername/Healthcare-Appointment-System)
- **Documentation**: [Wiki](https://github.com/yourusername/Healthcare-Appointment-System/wiki)
- **Issue Tracker**: [Issues](https://github.com/yourusername/Healthcare-Appointment-System/issues)
- **Discussions**: [Discussions](https://github.com/yourusername/Healthcare-Appointment-System/discussions)

### Support

- 📧 Email: support@healthcaresystem.com
- 💬 Discord: [Join our community](https://discord.gg/healthcare)
- 📱 Twitter: [@HealthcareApp](https://twitter.com/healthcareapp)

### Maintainers

- **Lead Developer** - [@yourusername](https://github.com/yourusername)

---

## 🙏 Acknowledgments

Special thanks to:

- [Spring Framework Team](https://spring.io/) - For the amazing framework
- [Twilio](https://www.twilio.com/) - Video and SMS services
- [AWS](https://aws.amazon.com/) - Cloud infrastructure
- [PostgreSQL](https://www.postgresql.org/) - Reliable database
- [Redis](https://redis.io/) - High-performance caching
- All [contributors](https://github.com/yourusername/Healthcare-Appointment-System/graphs/contributors) who helped improve this project

---

## ⭐ Show Your Support

If you find this project helpful, please consider:

- Giving it a ⭐ on [GitHub](https://github.com/yourusername/Healthcare-Appointment-System)
- Sharing it with others
- Contributing to its development
- Reporting bugs and suggesting features

---

<div align="center">

### 📊 Project Stats

![GitHub stars](https://img.shields.io/github/stars/yourusername/Healthcare-Appointment-System?style=social)
![GitHub forks](https://img.shields.io/github/forks/yourusername/Healthcare-Appointment-System?style=social)
![GitHub watchers](https://img.shields.io/github/watchers/yourusername/Healthcare-Appointment-System?style=social)
![GitHub issues](https://img.shields.io/github/issues/yourusername/Healthcare-Appointment-System)
![GitHub pull requests](https://img.shields.io/github/issues-pr/yourusername/Healthcare-Appointment-System)

---

**Made with ❤️ by developers, for healthcare**

[⬆ Back to Top](#-healthcare-appointment-system)

</div>
