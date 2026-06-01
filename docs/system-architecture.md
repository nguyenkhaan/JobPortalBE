# JobPortal Backend - System Architecture Overview

## 1. Architecture Pattern

The JobPortal backend follows a **Clean 3-Layer Architecture**:

```
┌─────────────────────────────────────────────────────────────┐
│                     HTTP Requests/Responses                  │
└─────────────────────────────────────────────────────────────┘
                             ↕
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│  Controllers (REST Endpoints, Request Validation)           │
│  - AuthController, JobPostController, ResumeController, etc  │
└─────────────────────────────────────────────────────────────┘
                             ↕
┌─────────────────────────────────────────────────────────────┐
│                     SERVICE LAYER                            │
│  Business Logic (Authorization, Domain Rules, Validation)   │
│  - AuthService, JobPostService, ResumeService, etc          │
└─────────────────────────────────────────────────────────────┘
                             ↕
┌─────────────────────────────────────────────────────────────┐
│                    PERSISTENCE LAYER                         │
│  Repositories (Data Access via Spring Data JPA)            │
│  - JobPostRepository, ResumeRepository, etc                 │
└─────────────────────────────────────────────────────────────┘
                             ↕
┌─────────────────────────────────────────────────────────────┐
│                   DATABASE LAYER                             │
│  PostgreSQL 16 with JPA/Hibernate ORM                       │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Core Modules & Responsibilities

### 2.1 Authentication Module (`auth`)
**Responsibility**: User registration, login, token management, password reset

**Components**:
- `AuthController`: Handles registration, login, token refresh endpoints
- `AuthService`: Business logic for user authentication
- `AuthRepository`: Data access for auth-related entities
- `JwtUtil`: JWT token generation and validation
- `UserDetailsServiceImpl`: Spring Security user loading

**Request Flow Example - Login**:
```
POST /auth/login
  → AuthController.login()
    → Extract email/password from request
    → AuthService.authenticate()
      → Find user by email
      → Verify password with BCrypt
      → Generate JWT tokens (ACCESS + REFRESH)
      → Return tokens to client
```

**Key Security**:
- Password hashing with BCrypt
- JWT tokens with expiration
- Token refresh mechanism
- Email verification for new accounts

---

### 2.2 User Management Module (`user`)
**Responsibility**: User listing and basic profile management

**Components**:
- `UserController`: User listing endpoint with pagination
- `UserService`: User query and profile management
- `UserRepository`: User data access

**Pagination Pattern**:
```
GET /user?limit=20&offset=1
  → Parameters validated (limit 1-100, offset ≥ 0)
  → Page number calculated: page = (offset - 1) / limit
  → Spring Data Pageable used for database query
  → Results wrapped in PageResponse<UserResponse>
```

---

### 2.3 Job Post Module (`jobpost`)
**Responsibility**: Job posting CRUD, filtering, search, status management

**Components**:
- `JobPostController`: REST endpoints for job management
- `JobPostService`: Business logic with complex filtering
- `JobPostRepository`: JPA repository with Specification<JobPost> for dynamic queries
- `JobPostFilterRequest`: Query filter DTO

**Advanced Features**:
- **Dynamic Filtering**: Specification-based queries for complex WHERE clauses
- **Multi-field Search**: Keyword search on title, salary range, education level, job level
- **Industry Filtering**: Join with JobIndustry table for multi-select filtering
- **Soft Delete**: Automatic filtering via @SQLRestriction
- **Pagination**: Manual offset/limit pagination for large datasets

**Request Flow Example - Search Jobs**:
```
GET /jobpost?keyword=senior&salaryMin=50000&educationLevel=BACHELOR
  → JobPostFilterRequest parsed
  → Specification<JobPost> built with dynamic predicates
  → Pageable created from limit/offset
  → Query executed with joins for industries
  → Results converted to JobPostResponse
  → Returns PageResponse<JobPostResponse>
```

---

### 2.4 Job Application Module (`jobapplication`)
**Responsibility**: Application tracking, status management, application history

**Components**:
- `JobApplicationController`: Application CRUD endpoints
- `JobApplicationService`: Application lifecycle management
- `JobApplicationRepository`: Data access with filters
- `JobApplicationResponse`/`JobApplicationDetailResponse`: Different response shapes for user vs admin

**Workflow**:
```
SEEKER creates application
  → Verify job post exists
  → Verify resume belongs to seeker
  → Prevent duplicate applications (unique constraint)
  → Create audit log entry
  
SEEKER updates application
  → Verify ownership (only owner can update)
  → Allow updating cover letter and resume
  → Prevent status changes (only ADMIN can change status)
  
ADMIN views application details
  → Can see full application with linked entities
  → Can update status (PENDING → REVIEWING → INTERVIEW → OFFER/REJECTED)
```

---

### 2.5 Resume Module (`resume`)
**Responsibility**: Resume upload/download, default resume management, file storage

**Components**:
- `ResumeController`: Upload, list, set default, delete endpoints
- `ResumeService`: Resume lifecycle with MinIO integration
- `ResumeRepository`: Resume data access
- `MinioService`: S3-compatible file storage integration

**File Storage Architecture**:
```
Upload Flow:
  → SEEKER uploads resume
  → Service stores file in MinIO (S3-compatible)
  → Database stores file URL (http://minio:9000/bucket/file)
  → Return file URL to frontend
  
Download Flow:
  → Frontend requests resume from URL
  → MinIO serves file directly
  → No proxying through backend
```

---

### 2.6 Employer Profile Module (`employer`)
**Responsibility**: Employer profile CRUD, logo management, approval workflow

**Components**:
- `EmployerController`: Profile endpoints with logo upload
- `EmployerService`: Profile management and validation
- `EmployerRepository`: Employer data access

**Profile Features**:
- Logo upload to MinIO
- Company information (name, website, address, email, phone)
- Subscription management (plan, amount, dates)
- Approval status tracking (PENDING/APPROVED/REJECTED)

---

### 2.7 Job Seeker Profile Module (`jobseeker`)
**Responsibility**: Job seeker profile CRUD, personal information management

**Components**:
- `JobSeekerController`: Profile endpoints
- `JobSeekerService`: Profile management
- `JobSeekerRepository`: Profile data access

**Profile Information**:
- Personal details (name, phone, DOB, gender)
- Professional info (title, bio, nationality)
- Experience & education summaries
- Approval status for platform

---

### 2.8 SavedCandidate Module (`savedcandidate`) - NEW
**Responsibility**: Employers saving job seekers for future reference

**Components**:
- `SavedCandidateController`: Secured with EMPLOYER role
- `SavedCandidateService`: Save/list/remove logic
- `SavedCandidateRepository`: Data access

**Key Constraint**:
- Unique index on (employer_id, job_seeker_id) prevents duplicates
- Automatic duplicate detection prevents re-saves

---

### 2.9 Notification Module (`notification`) - NEW
**Responsibility**: User notification management and delivery

**Components**:
- `NotificationController`: Notification endpoints
- `NotificationService`: Notification lifecycle
- `NotificationRepository`: Data access with read status queries

**Use Cases**:
- Application status updates
- Employer profile approvals
- Job post related notifications
- System alerts

---

### 2.10 DeviceToken Module (`devicetoken`) - NEW
**Responsibility**: Multi-device push notification support

**Components**:
- `DeviceTokenController`: Device registration endpoints
- `DeviceTokenService`: Device lifecycle
- `DeviceTokenRepository`: Device data access

**Mobile Integration**:
- Web, iOS, Android device identification
- Last active timestamp tracking
- Automatic duplicate token replacement

---

### 2.11 Payment Module (`payment`) - NEW
**Responsibility**: Payment processing and subscription management

**Components**:
- `PaymentController`: Payment endpoints (EMPLOYER and ADMIN)
- `PaymentService`: Payment lifecycle
- `PaymentRepository`: Payment data access

**Payment Workflow**:
```
EMPLOYER creates payment
  → Service generates transaction reference (UUID)
  → Creates Payment record with PENDING status
  → Frontend initiates payment via MOMO gateway
  
MOMO callback received
  → Payment status updated to COMPLETED/FAILED
  → Employer subscription/plan updated (future)
  
ADMIN views payment history
  → Can update payment status if needed
```

---

### 2.12 Industry Module (`industry`)
**Responsibility**: Job industry taxonomy (admin-only)

**Components**:
- `IndustryController`: CRUD endpoints (ADMIN only)
- `IndustryService`: Industry management
- `IndustryRepository`: Industry data access

---

### 2.13 Security & Configuration
**Components**:
- `SecurityConfig`: Spring Security configuration, JWT filter chain
- `AuthenticationJwtFilter`: JWT extraction and validation
- `UserDetailsImpl`: User details with roles for Spring Security
- `GlobalException`: Centralized exception handling with proper HTTP status codes

---

## 3. Data Flow & Request Examples

### 3.1 Complete User Journey - Job Application

```
1. SEEKER Registration & Login
   POST /auth/register
   → Email verification
   → Login with credentials
   → Receive JWT tokens

2. Create Job Seeker Profile
   POST /jobseeker
   → Store personal & professional info
   → Backend stores user ID link

3. Upload Resume(s)
   POST /resumes/upload (multipart/form-data)
   → File stored in MinIO
   → Database stores file reference
   → Set default resume

4. Search & Browse Jobs
   GET /jobpost?keyword=senior&salaryMin=50000
   → Complex query with filtering
   → Get list with pagination

5. View Job Details
   GET /jobpost/{id}
   → Get full job post with industries
   → Related employer info

6. Apply for Job
   POST /job-application
   {
     "jobPostId": 123,
     "resumeId": 456,
     "coverLetter": "..."
   }
   → Verify job exists
   → Verify resume ownership
   → Check no duplicate application
   → Create audit log
   → Return application ID

7. Track Application
   GET /job-application/{id}
   → View current application status
   → See updates as employer reviews

8. Receive Notifications
   GET /notifications
   → See status updates
   → Mark as read
```

### 3.2 Employer Journey - Job Posting to Hiring

```
1. Employer Registration
   POST /auth/register (type: EMPLOYER)
   → Register user
   → Get JWT tokens

2. Create Employer Profile
   POST /employer
   {
     "companyName": "TechCorp",
     "logo": <file>,
     "companyWebsite": "...",
     ...
   }
   → Upload logo to MinIO
   → Store company info
   → Initial approval status: PENDING

3. Approval Workflow
   ADMIN reviews employer profile
   PATCH /employer/{id}/status?status=APPROVED
   → Employer notified
   → Can now post jobs

4. Create Job Post
   POST /jobpost
   {
     "title": "Senior Developer",
     "description": "...",
     "industryIds": [1, 2, 3],
     ...
   }
   → Service verifies employer has approved profile
   → Creates job post with OPEN status
   → Associates with industries

5. View Applications
   GET /job-application?jobPostId=456
   → List all applications for this job
   → See seeker info and resumes

6. Review Application
   GET /job-application/{appId}
   → View complete application details
   → View resume file
   → Read cover letter

7. Update Application Status
   PATCH /job-application/{appId}
   {
     "status": "INTERVIEW"
   }
   → Update workflow status
   → Seeker receives notification
   → Create audit log

8. Save Promising Candidates
   POST /saved-candidates
   {
     "jobSeekerId": 789
   }
   → Save seeker for future reference
   → Use for future job postings

9. Make Payment
   POST /payments
   {
     "planName": "premium",
     "cost": 99.99
   }
   → Create payment record
   → Frontend initiates MOMO payment
   → Update subscription on success
```

---

## 4. Database Schema Overview

### 4.1 Entity Relationships

```
User (Core Identity)
├── 1:1 → JobSeekerProfile
├── 1:1 → EmployerProfile
├── 1:N → UserRole
├── 1:N → Resume
├── 1:N → Token
├── 1:N → DeviceToken
├── 1:N → Notification
├── 1:N → Payment
├── 1:N → Social
├── 1:N → OAuth
└── 1:N → AuditLog

EmployerProfile
├── 1:N → JobPost
└── 1:N → SavedCandidate

JobSeekerProfile
├── 1:N → Resume
└── 1:N → SavedCandidate

JobPost
├── 1:N → JobApplication
└── 1:N → JobIndustry

JobApplication
├── M:1 → JobSeekerProfile
├── M:1 → JobPost
└── M:1 → Resume

Industry
└── 1:N → JobIndustry
```

### 4.2 Soft Delete Pattern

All domain entities support soft delete:
```java
@SQLDelete(sql = "UPDATE entity SET delete_at = NOW() WHERE id = ?")
@SQLRestriction("delete_at is NULL")
public class Entity {
    @Column(name = "delete_at")
    @Builder.Default
    private LocalDateTime deleteAt = null;
}
```

**Benefits**:
- Data recovery capability
- Audit trail preservation
- Referential integrity without cascade deletes
- Transparent to business logic via @SQLRestriction

---

## 5. API Response Standards

### 5.1 Successful Response
```json
{
  "success": true,
  "message": "Optional message",
  "data": {}
}
```

### 5.2 Error Response
```json
{
  "success": false,
  "message": "Error description",
  "error": "ERROR_CODE"
}
```

### 5.3 Paginated Response
```json
{
  "success": true,
  "data": {
    "items": [ {} ],
    "totalItems": 100,
    "page": 0,
    "size": 20
  }
}
```

---

## 6. Authentication & Authorization

### 6.1 JWT Token Flow
```
Client Login
  → POST /auth/login
    → Server validates credentials
    → Generates ACCESS token (short-lived)
    → Generates REFRESH token (long-lived)
    → Returns both tokens

Client API Request
  → Include Authorization: Bearer <ACCESS_TOKEN>
  → JwtAuthenticationFilter validates token
  → UserDetailsServiceImpl loads user info
  → Spring Security sets authentication context
  → @PreAuthorize checks roles
  → Controller method executes with user info
```

### 6.2 Role-Based Access Control
```
@PreAuthorize("hasRole('EMPLOYER')")  → Only employers
@PreAuthorize("hasRole('SEEKER')")    → Only job seekers
@PreAuthorize("hasRole('ADMIN')")     → Only admins
No annotation                          → Any authenticated user
```

### 6.3 Authorization Checks in Services
```java
// Extract user from JWT
Long userId = UserDetailsImpl.getId();

// Get user's employer profile
EmployerProfile employer = employerRepository.findByOwnerId(userId)
    .orElseThrow(() -> new NotFoundException("Not an employer"));

// Verify ownership before modifying
if (!employer.getId().equals(jobPost.getEmployerId())) {
    throw new ForbiddenException("Not authorized");
}
```

---

## 7. Error Handling

### 7.1 Custom Exceptions
- `BadRequestException` (400): Invalid input
- `NotFoundException` (404): Resource not found
- `UnauthorizedException` (401): Missing authentication
- `ForbiddenException` (403): Insufficient permissions

### 7.2 Global Exception Handler
```java
@ControllerAdvice
public class GlobalException {
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(...) {
        return ResponseEntity.badRequest().body(...);
    }
    // ... other handlers
}
```

---

## 8. Performance Optimization

### 8.1 Database Indexing
- Indices on commonly filtered columns (status, employment_type, job_level)
- Unique constraints on frequently checked values
- Composite indices for complex queries

### 8.2 Pagination
- Offset-based pagination for large datasets
- Configurable page size (max 100 items)
- Prevents loading entire result sets

### 8.3 Lazy Loading
- ManyToOne relationships use `FetchType.LAZY`
- Prevents N+1 queries
- Service layer ensures session scope for lazy loading

### 8.4 Caching (Future)
- Token validation caching
- User role caching
- Industry list caching

---

## 9. External Integrations

### 9.1 MinIO (File Storage)
- S3-compatible object storage
- Resume and employer logo storage
- Public bucket configuration for direct download

### 9.2 MOMO Payment Gateway
- Payment initiation from frontend
- Callback handling for payment status
- Transaction reference tracking

### 9.3 Email Service (SMTP)
- Account verification emails
- Password reset emails
- Notification emails

### 9.4 OAuth2 (Google) - Planned
- Google login integration
- Third-party authentication

---

## 10. Deployment Architecture

### 10.1 Docker Container
```dockerfile
FROM gradle:8.7-jdk17 AS build
WORKDIR /app
COPY . .
RUN ./gradlew clean build

FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 10.2 Docker Compose
```yaml
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_PASSWORD: password
    volumes:
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql

  minio:
    image: minio/minio
    ports:
      - "9000:9000"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin

  backend:
    build: .
    ports:
      - "8080:8080"
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/jobportal
      MINIO_URL: http://minio:9000
    depends_on:
      - postgres
      - minio
```

---

## 11. Development Workflow

### 11.1 Setup
```bash
# Clone repository
git clone <repo>
cd JobPortal

# Build project
./gradlew clean build

# Start Docker containers
docker-compose up -d

# Run application
./gradlew bootRun
```

### 11.2 Database Initialization
- Scripts in `init.sql` and `__init__.sql`
- Spring JPA creates schema if needed
- DataSeeder populates test data

### 11.3 Testing
```bash
# Unit tests
./gradlew test

# Integration tests
./gradlew integrationTest

# Build without tests
./gradlew build -x test
```

---

## 12. Key Design Decisions

### 12.1 3-Layer Architecture
**Why**: Clean separation of concerns, testability, maintainability
**Benefit**: Easy to modify one layer without affecting others

### 12.2 Soft Delete Pattern
**Why**: Audit trail preservation, data recovery, compliance
**Benefit**: No data loss, can query deleted records, single table queries

### 12.3 Manual Pagination
**Why**: Consistent with existing JobPost module
**Benefit**: Simpler API contract, predictable behavior

### 12.4 DTOs for API Contracts
**Why**: Decouple domain models from API responses
**Benefit**: Can change database schema without breaking API

### 12.5 Service Layer Authorization
**Why**: Business-level authorization checks
**Benefit**: Ensures consistency, prevents bypass via different endpoints

---

## 13. Monitoring & Logging

### 13.1 Audit Logging
```java
AuditService.log(
    ActionType.CREATE,
    EntityName.JobPost,
    jobPost.getId(),
    userId,
    jobPostData
);
```

**Captures**:
- Action type (CREATE, UPDATE, DELETE, etc.)
- Entity type and ID
- User performing action
- Timestamp
- JSON data for review

### 13.2 Application Logging
- DEBUG: Development details
- INFO: Important lifecycle events
- WARN: Potential issues
- ERROR: Exceptions and failures

---

## 14. Future Enhancements

### 14.1 Planned Features
- Full-text search for jobs
- Job recommendations via ML
- Messaging system between parties
- Advanced analytics dashboard
- Employer billing & subscription management

### 14.2 Potential Optimizations
- Redis caching layer
- Elasticsearch for job search
- Message queue for async operations
- GraphQL API alternative

### 14.3 Security Hardening
- Rate limiting on auth endpoints
- CSRF protection
- API key management
- Request validation rules

---

## Summary

The JobPortal backend architecture provides:
- ✅ Clean, maintainable code structure
- ✅ Comprehensive authorization and security
- ✅ Flexible data access with soft deletes
- ✅ Scalable module-based design
- ✅ Consistent API responses
- ✅ Production-ready error handling
- ✅ Database integrity through constraints
- ✅ Easy integration with third-party services

The system is designed for scalability, maintainability, and extensibility while maintaining security and data integrity.
