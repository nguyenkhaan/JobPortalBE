# System Architecture Overview

## Architecture Pattern

The Job Portal follows a **3-Layer Architecture** pattern:

```
Controller Layer → Service Layer → Repository Layer → Database
```

Each request flows through these layers with clear separation of concerns.

---

## Main Components & Responsibilities

### Controllers (REST API Endpoints)
- Handle HTTP requests/responses
- Validate user authentication via `@PreAuthorize`
- Extract user info from JWT token
- **Key Controllers**: `AuthController`, `JobPostController`, `JobApplicationController`, `ResumeController`, `JobSeekerController`, `EmployerController`

### Services (Business Logic)
- Implement core business rules and validations
- Handle authorization checks (user ownership, admin verification)
- Interact with repositories to fetch/persist data
- Manage transactions with `@Transactional`
- Create audit logs for important operations
- **Examples**: `AuthService`, `JobApplicationService`, `ResumeService`, `JobPostService`

### Repositories (Data Access)
- CRUD operations using Spring Data JPA
- Custom query methods with `JpaSpecificationExecutor` for complex filters
- **Pattern**: `extends JpaRepository<Entity, Long>`

### Models (Database Entities)
- JPA entities with Lombok annotations
- Relationships: One-to-Many, Many-to-One, One-to-One
- Soft delete support via `@SQLDelete` and `@SQLRestriction`

---

## Request Flow Example: Create Job Application

```
POST /api/job-application
    ↓
JobApplicationController.createJobApplication()
    - Extract userId from JWT token
    - Validate @Valid CreateJobApplicationDto
    ↓
JobApplicationService.createJobApplication(userId, dto)
    - Check if job post exists
    - Verify resume belongs to user
    - Validate no duplicate application
    - Create audit log
    ↓
JobApplicationRepository.save(jobApplication)
    ↓
Database (PostgreSQL)
```

---

## Authentication & Authorization

### JWT Token Flow
1. **Login**: `AuthService.login()` generates ACCESS and REFRESH tokens
2. **Token Validation**: `JwtAuthenticationFilter` extracts token from `Authorization: Bearer <token>` header
3. **User Loading**: `UserDetailsServiceImpl` loads user details from database
4. **Spring Security**: Sets authentication context for `@PreAuthorize` checks

### Role-Based Access Control (RBAC)
- Roles stored in `UserRole` entity (many-to-many with User)
- Common roles: `ADMIN`, `EMPLOYER`, `SEEKER`
- Annotation-based: `@PreAuthorize("hasRole('ADMIN')")`

### Exception Handling
- Global exception handler via `@ControllerAdvice`
- Custom exceptions: `BadRequestException`, `NotFoundException`, `UnauthorizedException`, `ForbiddenException`
- Returns `ExceptionDto` with error message and HTTP status

---

## Key Technologies & Why They're Used

### Spring Boot 4.0.3
- **Why**: Rapid enterprise application development with embedded Tomcat
- **Benefits**: Auto-configuration, dependency injection, built-in security

### Spring Security 6
- **Why**: Authentication and authorization framework
- **Benefits**: JWT support, role-based access control, password encryption (BCrypt)

### Spring Data JPA
- **Why**: Object-relational mapping and repository pattern
- **Benefits**: Automatic CRUD operations, custom queries, pagination support

### PostgreSQL 16
- **Why**: Robust relational database
- **Benefits**: ACID compliance, JSON support, advanced indexing

### JWT (JSON Web Tokens)
- **Why**: Stateless authentication for RESTful APIs
- **Benefits**: No server-side session storage, scalable, secure token-based auth
- **Library**: JJWT 0.12.5

### Lombok
- **Why**: Reduces boilerplate code (getters, setters, constructors)
- **Benefits**: Cleaner model classes, less maintenance
- **Annotations**: `@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`

### MinIO 8.5.2
- **Why**: S3-compatible object storage for file uploads
- **Benefits**: Resume/document storage, file URL generation, scalable file management

### Spring Mail
- **Why**: Send email notifications (password reset, verification)
- **Benefits**: Template support via Thymeleaf, integration with Mailpit for testing

### Validation Framework (Jakarta Validation 4.1.0)
- **Why**: Input validation at controller and DTO level
- **Benefits**: Automatic constraint checking, custom error messages
- **Annotations**: `@NotNull`, `@NotBlank`, `@Email`, `@Min`, `@Valid`

### SpringDoc OpenAPI (Swagger)
- **Why**: API documentation and testing interface
- **Benefits**: Auto-generated API docs, Scalar UI at `/docs`

---

## Infrastructure & Deployment

### Docker Compose
Containerized services for local development:
- **PostgreSQL**: Main database
- **Adminer**: Database administration UI
- **Mailpit**: Email testing (SMTP + Web UI)
- **MinIO**: Object storage server

### Dockerfile
- **Build Stage**: Gradle build on Java 17
- **Runtime Stage**: Eclipse Temurin JRE 17
- **Optimization**: Multi-stage build for smaller image size

### Environment Variables
Configured via `.env` file:
- Database credentials
- JWT secrets
- MinIO credentials
- Mail configuration
- Frontend URL (CORS)

---

## Database Schema Highlights

### Key Entities
- **User**: Core user account (email, password, active status)
- **UserRole**: Maps users to roles (ADMIN, EMPLOYER, SEEKER)
- **EmployerProfile**: Company details, job postings
- **JobSeekerProfile**: Resume, applications, work experience
- **JobPost**: Job listings with salary, requirements, industries
- **JobApplication**: Seeker applications to job posts
- **Resume**: Uploaded files stored in MinIO with URLs
- **AuditLog**: Tracks all significant operations (created by, action, timestamp)

### Soft Delete Pattern
All entities support soft delete:
```sql
UPDATE table_name SET delete_at = NOW() WHERE id = ?
WHERE delete_at IS NULL
```

---

## API Response Format

### Success Response
```json
{
  "success": true,
  "data": { "id": 1, "name": "John" }
}
```

### Paginated Response
```json
{
  "success": true,
  "data": {
    "items": [],
    "totalItems": 100,
    "page": 0,
    "size": 20
  }
}
```

### Error Response
```json
{
  "message": "Not found",
  "code": 404
}
```

---

## Module Structure

Each feature module follows this structure:
```
module/
├── {Module}Controller.java          # REST endpoints
├── {Module}Service.java             # Business logic
├── {Module}Repository.java          # Data access
└── dto/
    ├── Create{Entity}Dto.java       # Request DTO
    ├── Update{Entity}Dto.java       # Update DTO
    └── {Entity}Response.java        # Response DTO
```

**Active Modules**: auth, job-post, job-application, job-seeker, employer, resume, industry, payment, audit, token, email, minio

---

## Security Highlights

✅ **Implemented**:
- JWT token-based authentication
- Role-based authorization
- Password hashing (BCrypt)
- CORS configuration
- Soft delete (no permanent data loss)
- Audit logging for sensitive operations
- Input validation via Jakarta Validation

⚠️ **In Progress/Notes**:
- Some controllers still mixing response formats (migration to `ApiResponse` standard ongoing)
- Admin role guards need review before production
