# API Response + JobApplication Changes

This document summarizes the API consistency/refactor work applied to the project, focusing on:

- Completing missing **Job Application** features (update/detail/delete)
- Refactoring selected endpoints to return **frontend-friendly DTOs**
- Introducing a **standard response envelope** and **standard pagination shape**

---

## 1) Standard response envelope

### `ApiResponse<T>`

Added `ApiResponse<T>` to standardize successful responses:

- `success`: boolean (defaults to `true`)
- `message`: optional string
- `data`: payload of type `T`

Location:

- `src/main/java/Cloudian/JobPortal/modules/base/dto/ApiResponse.java`

Successful responses now commonly look like:

```json
{
  "success": true,
  "message": "Optional message",
  "data": { }
}
```

---

## 2) Standard pagination response shape

### `PageResponse<T>`

Added `PageResponse<T>` to avoid leaking Spring `Page` internals and to keep pagination predictable for frontend rendering:

- `items`: list of items
- `totalItems`: total number of records
- `page`: zero-based page index (from Spring `Page#getNumber()`)
- `size`: page size

Location:

- `src/main/java/Cloudian/JobPortal/modules/base/dto/PageResponse.java`

Paginated responses now commonly look like:

```json
{
  "success": true,
  "data": {
    "items": [],
    "totalItems": 0,
    "page": 0,
    "size": 20
  }
}
```

---

## 3) Job Application module updates

### Endpoints

Base path: `job-application`

- `POST /job-application`
  - **Role**: `SEEKER`
  - **Behavior**: Create a job application for a job post
  - **Response**: `ApiResponse<JobApplicationResponse>`
  - **Important**: No longer returns JPA entity directly

- `GET /job-application`
  - **Behavior**: List job applications
  - **Response**: `ApiResponse<PageResponse<JobApplicationResponse>>`

- `PATCH /job-application/{id}`
  - **Role**: `SEEKER`
  - **Behavior**: Update cover letter and/or resume for the application
  - **Authorization**: Only the owner (job seeker) can update; otherwise `403`
  - **Response**: `ApiResponse<JobApplicationResponse>`

- `GET /job-application/{id}`
  - **Role**: `ADMIN`
  - **Behavior**: Admin-only detailed view
  - **Response**: `ApiResponse<JobApplicationDetailResponse>`

- `DELETE /job-application/{id}`
  - **Role**: `ADMIN`
  - **Behavior**: Admin-only delete (soft-delete at entity level)
  - **Response**: `204 No Content`

### DTOs added/updated

- **Added** `UpdateJobApplicationDto`
  - Location: `src/main/java/Cloudian/JobPortal/modules/jobapplication/dto/UpdateJobApplicationDto.java`

- **Added** `JobApplicationDetailResponse`
  - Includes: `jobSeekerProfile`, `jobPost` summary, `resume` summary, and `appliedAt`
  - Location: `src/main/java/Cloudian/JobPortal/modules/jobapplication/dto/JobApplicationDetailResponse.java`

- **Added** summaries (nested DTO safety)
  - `JobApplicationJobPostSummaryResponse`
  - `JobApplicationResumeSummaryResponse`

- **Updated** `JobApplicationResponse`
  - Now includes `jobPost` summary so frontend can render application list with job title
  - Location: `src/main/java/Cloudian/JobPortal/modules/jobapplication/dto/JobApplicationResponse.java`

### Validation fix

- Removed invalid `@NotBlank` on `Long jobPostId` in `CreateJobApplicationDto`
  - `@NotBlank` is for strings; `@NotNull` is correct for `Long`

Files:

- `src/main/java/Cloudian/JobPortal/modules/jobapplication/dto/CreateJobApplicationDto.java`

---

## 4) Avoid returning entities directly (selected fixes applied)

### Audit logs

Previously, the audit list endpoint returned a `Page<AuditLog>` which can expose nested `User` objects and internal JSON fields.

Changes:

- `AuditController` now returns `ApiResponse<PageResponse<AuditLogResponse>>`
- Introduced `AuditLogResponse` to expose only safe frontend fields

Files:

- `src/main/java/Cloudian/JobPortal/modules/audit/AuditController.java`
- `src/main/java/Cloudian/JobPortal/modules/audit/AuditService.java`
- `src/main/java/Cloudian/JobPortal/modules/audit/dto/AuditLogResponse.java`

### Users

- `UserController` now returns `ApiResponse<List<UserResponse>>`

File:

- `src/main/java/Cloudian/JobPortal/modules/user/UserController.java`

---

## 5) Notes / follow-ups

- **Validation errors** are currently returned as a raw map `{ fieldName: message }` from `GlobalException#handleValidationExceptions`.
  - If desired, this can be standardized into `ApiResponse` as well for full consistency.

- Several controllers still return mixed shapes (`HashMap`, raw DTOs, or `ResponseEntity<?>`).
  - Suggested next step: migrate remaining controllers (`AuthController`, `IndustryController`, `EmployerController`, etc.) to `ApiResponse` + `PageResponse` where appropriate.

