# 💼 Job Portal Server

A comprehensive job portal backend application built with Spring Boot, enabling job seekers and employers to connect seamlessly.

---

## 🛠️ Tech Stack

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-6DB33F?style=flat-square&logo=spring-boot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?style=flat-square&logo=postgresql)
![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-5.0.3-6DB33F?style=flat-square&logo=spring)
![Spring Security](https://img.shields.io/badge/Spring%20Security-5.0.3-6DB33F?style=flat-square&logo=spring)
![Lombok](https://img.shields.io/badge/Lombok-1.18-FF6B6B?style=flat-square&logo=java)
![Docker](https://img.shields.io/badge/Docker-Latest-2496ED?style=flat-square&logo=docker)
![Gradle](https://img.shields.io/badge/Gradle-Build%20System-02303A?style=flat-square&logo=gradle)

---

## 📊 Database Schema

```mermaid
erDiagram
    USERS ||--o{ JOB_SEEKER_PROFILE : owns
    USERS ||--o{ EMPLOYER_PROFILE : owns
    USERS ||--o{ USERS_ROLE : has
    USERS ||--o{ TOKEN : generates
    EMPLOYER_PROFILE ||--o{ JOB_POST : creates
    JOB_POST ||--o{ JOB_APPLICATION : receives
    JOB_POST ||--o{ JOB_INDUSTRY : categorized_by
    JOB_SEEKER_PROFILE ||--o{ JOB_APPLICATION : submits
    JOB_SEEKER_PROFILE ||--o{ RESUME : uploads
    RESUME ||--o{ JOB_APPLICATION : attached_to
    INDUSTRY ||--o{ JOB_INDUSTRY : belongs_to

    USERS {
        long id PK
        string email UK
        string password
        boolean active
        timestamp created_at
        timestamp updated_at
    }

    JOB_SEEKER_PROFILE {
        long id PK
        string fullName
        string address
        string phone
        long usersID FK
    }

    EMPLOYER_PROFILE {
        long id PK
        string companyName
        string companyWebsite
        string address
        string email
        string description
        string phone
        integer capacity
        long owner_id FK
    }

    USERS_ROLE {
        long id PK
        string role "SEEKER, EMPLOYER, ADMIN"
        long users_id FK
    }

    TOKEN {
        long id PK
        string token
        string type
        long users_id FK
        timestamp created_at
        timestamp used_at
        timestamp expires_at
    }

    JOB_POST {
        long id PK
        string title
        string description
        string employment_type
        string status "OPEN, CLOSED"
        decimal salary_min
        decimal salary_max
        timestamp created_at
        long employer_id FK
    }

    JOB_APPLICATION {
        long id PK
        string cover_letter
        string status "PENDING, ACCEPTED, REJECTED"
        timestamp applied_at
        long job_seeker_id FK
        long job_post_id FK
        long resume_id FK
    }

    RESUME {
        long id PK
        string file_url
        boolean default_resume
        timestamp uploaded_at
        long job_seeker_id FK
    }

    INDUSTRY {
        long id PK
        string name
    }

    JOB_INDUSTRY {
        long id PK
        long industry_id FK
        long job_post_id FK
    }
```

---

## 🚀 How to Run

### Prerequisites
- Docker and Docker Compose installed on your machine
- Java 17+ (if running without Docker)
- PostgreSQL 16+ (if running without Docker)

### Option 1: Using Docker Compose (Recommended)

1. **Clone the repository**
   ```bash
   cd JobPortal
   ```

2. **Build and start services**
   ```bash
   docker-compose up --build
   ```

   This will start:
   - PostgreSQL database on `localhost:5432`
   - Adminer (database UI) on `localhost:5050`
   - Spring Boot application on `localhost:8080`

3. **Verify services are running**
   - Spring Boot API: http://localhost:8080
   - Adminer: http://localhost:5050
   - Database credentials:
     - Username: `admin`
     - Password: `admin`
     - Database: `mydb`

4. **Stop services**
   ```bash
   docker-compose down
   ```

### Option 2: Running Locally

1. **Start PostgreSQL Database**
   ```bash
   # Ensure PostgreSQL is running on localhost:5432
   # Create database 'mydb' with user 'admin' and password 'admin'
   ```

2. **Build the project**
   ```bash
   ./gradlew clean build
   ```

3. **Run the Spring Boot application**
   ```bash
   ./gradlew bootRun
   ```

   The application will start on `http://localhost:8080`

### Database Configuration
- **URL**: `jdbc:postgresql://localhost:5432/mydb`
- **Username**: `admin`
- **Password**: `admin`
- **DDL Strategy**: `update` (automatically creates/updates tables)

---

## 📚 API Modules

- **Authentication** - User registration, login, token management
- **User Management** - User profile and role management
- **Job Management** - Job post creation, searching, and management
- **Applications** - Job application submission and tracking
- **Resume** - Resume upload and management

---

## 🔧 Development Notes

- **ORM**: Hibernate via Spring Data JPA
- **Validation**: Jakarta validation annotations
- **Dependency Injection**: Spring Framework
- **Database Indexes**: Optimized for common query patterns
- **Timezone**: Using LocalDateTime for all timestamps

---

## 📝 License

This project is part of the Cloudian Job Portal initiative 