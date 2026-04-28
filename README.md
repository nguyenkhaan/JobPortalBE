# 💼 Job Portal Server

> A comprehensive job portal backend application built with Spring Boot, enabling job seekers and employers to connect seamlessly.

---

## 🛠️ Tech Stack

<div align="center">

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?style=for-the-badge&logo=postgresql&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-5.0.3-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-5.0.3-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Lombok](https://img.shields.io/badge/Lombok-1.18-FF6B6B?style=for-the-badge&logo=java&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Latest-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-Build%20System-02303A?style=for-the-badge&logo=gradle&logoColor=white)

</div>

---

## 📊 Database Schema

```mermaid
erDiagram
    USERS ||--o{ JOB_SEEKER_PROFILE : has
    USERS ||--o{ EMPLOYER_PROFILE : owns
    USERS ||--o{ USER_ROLE : "has roles"
    USERS ||--o{ TOKEN : generates
    USERS ||--o{ SOCIAL : "has accounts"
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
        long userId FK
    }

    EMPLOYER_PROFILE {
        long id PK
        string logo
        string companyName
        string companyWebsite
        string address
        string email
        text description
        string phone
        integer capacity
        long owner_id FK
    }

    USER_ROLE {
        long id PK
        enum role "SEEKER, EMPLOYER, ADMIN"
        long user_id FK
    }

    TOKEN {
        long id PK
        string token
        enum type
        long user_id FK
        timestamp created_at
        timestamp used_at
        timestamp expires_at
    }

    SOCIAL {
        long id PK
        string title
        string social_link
        long user_id FK
    }

    JOB_POST {
        long id PK
        string title
        text description
        enum employment_type "FULLTIME, PARTTIME, CONTRACT"
        enum status "OPEN, CLOSED"
        enum education_level "BACHELOR, MASTER, PHD"
        enum job_level "INTERN, JUNIOR, SENIOR, MANAGER"
        integer experience
        decimal salary_min
        decimal salary_max
        timestamp created_at
        long employer_id FK
    }

    JOB_APPLICATION {
        long id PK
        string cover_letter
        enum status "PENDING, ACCEPTED, REJECTED"
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
- IntelliJ IDEA (or VS Code with Java extensions)

### 1. Clone the repository

```bash
cd JobPortal
```

### 2. Build and start services

Check the services in `docker-compose.yaml`

```bash
docker-compose up -d postgres adminer 
```

**⚠️ Notice**: Don't run the app service in `docker-compose.yaml`

This will start:
- PostgreSQL database on `localhost:5432`
- Adminer (database UI) on `localhost:5050`
- Spring Boot application on `localhost:8080`

### 3. Verify services are running

- **Spring Boot API**: http://localhost:8080
- **Adminer**: http://localhost:5050
- **Database Credentials**: (http://localhost:5432)
  - Username: `admin`
  - Password: `admin`
  - Database: `mydb`

- **Account for login Adminer** (http://localhost:5050) 
  - Database: PostgresSQL 
  - Server: postgres 
  - username: admin 
  - password: admin 
  - database: mydb 
### 4. Stop services (if needed)

```bash
docker-compose down
```

**⚠️ Notice!**: Don't run the app in docker-compose automatically.

### 5. Run the Spring Boot application

- Add environment variables from `.env.example` to Application Configuration
- Update `application.properties` file: change `spring.jpa.hibernate.ddl-auto=` to `create` (if you're creating the database for the first time)
- Click the **Run** button on IntelliJ IDEA to start the application
- The application will start on `http://localhost:8080`

### Database Configuration

| Property | Value |
|----------|-------|
| **URL** | `jdbc:postgresql://localhost:5432/mydb` |
| **Username** | `admin` |
| **Password** | `admin` |
| **DDL Strategy** | `update` |

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

<div align="center">

**Made with Cloudian 💙 with love**

</div>

