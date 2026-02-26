-- ============================================================================
-- Job Portal Database Seed File
-- ============================================================================
-- This file contains initial data for the Job Portal application.
-- It should be executed after the schema creation.
-- The Hibernate DDL-auto property should be set to "create" or "validate" 
-- to avoid conflicts during initialization.
-- 1. INSERT INDUSTRY DATA
-- ============================================================================
INSERT INTO industry (id, name) VALUES
(1, 'Information Technology'),
(2, 'Finance'),
(3, 'Healthcare'),
(4, 'Retail'),
(5, 'Manufacturing'),
(6, 'Education'),
(7, 'Marketing'),
(8, 'Human Resources');

-- ============================================================================
-- 2. INSERT USERS DATA
-- ============================================================================
-- Note: Password "admin" is encoded using BCrypt with strength 10
-- Original password: admin
-- BCrypt Hash: $2a$10$slYQmyNdGzin7olVN3/p2OPST9/PgBkqquzi.Ss7KIUgO2t0jKMUm
-- ============================================================================

-- Admin User (ADMIN role)
INSERT INTO users (id, email, password, active, created_at, updated_at) VALUES
(1, 'admin@gmail.com', '$2a$10$slYQmyNdGzin7olVN3/p2OPST9/PgBkqquzi.Ss7KIUgO2t0jKMUm', true, NOW(), NOW());

-- Sample Job Seeker Users
INSERT INTO users (id, email, password, active, created_at, updated_at) VALUES
(2, 'john.doe@gmail.com', '$2a$10$slYQmyNdGzin7olVN3/p2OPST9/PgBkqquzi.Ss7KIUgO2t0jKMUm', true, NOW(), NOW()),
(3, 'jane.smith@gmail.com', '$2a$10$slYQmyNdGzin7olVN3/p2OPST9/PgBkqquzi.Ss7KIUgO2t0jKMUm', true, NOW(), NOW()),
(4, 'michael.johnson@gmail.com', '$2a$10$slYQmyNdGzin7olVN3/p2OPST9/PgBkqquzi.Ss7KIUgO2t0jKMUm', true, NOW(), NOW()),
(5, 'sarah.williams@gmail.com', '$2a$10$slYQmyNdGzin7olVN3/p2OPST9/PgBkqquzi.Ss7KIUgO2t0jKMUm', true, NOW(), NOW());

-- Sample Employer Users
INSERT INTO users (id, email, password, active, created_at, updated_at) VALUES
(6, 'manager1@techcorp.com', '$2a$10$slYQmyNdGzin7olVN3/p2OPST9/PgBkqquzi.Ss7KIUgO2t0jKMUm', true, NOW(), NOW()),
(7, 'manager2@financecorp.com', '$2a$10$slYQmyNdGzin7olVN3/p2OPST9/PgBkqquzi.Ss7KIUgO2t0jKMUm', true, NOW(), NOW());

-- ============================================================================
-- 3. INSERT USERS_ROLE DATA
-- ============================================================================
-- Admin role for admin user
INSERT INTO users_role (id, role, users_id) VALUES
(1, 'ADMIN', 1);

-- SEEKER roles for job seekers
INSERT INTO users_role (id, role, users_id) VALUES
(2, 'SEEKER', 2),
(3, 'SEEKER', 3),
(4, 'SEEKER', 4),
(5, 'SEEKER', 5);

-- EMPLOYER roles for employers
INSERT INTO users_role (id, role, users_id) VALUES
(6, 'EMPLOYER', 6),
(7, 'EMPLOYER', 7);

-- ============================================================================
-- 4. INSERT JOB_SEEKER_PROFILE DATA
-- ============================================================================
INSERT INTO job_seeker_profile (id, full_name, address, phone, usersID) VALUES
(1, 'John Doe', '123 Main Street, New York, NY 10001', '555-0101', 2),
(2, 'Jane Smith', '456 Oak Avenue, Los Angeles, CA 90001', '555-0102', 3),
(3, 'Michael Johnson', '789 Pine Road, Chicago, IL 60601', '555-0103', 4),
(4, 'Sarah Williams', '321 Elm Street, Houston, TX 77001', '555-0104', 5);

-- ============================================================================
-- 5. INSERT EMPLOYER_PROFILE DATA
-- ============================================================================
INSERT INTO employer_profile (id, owner_id, company_name, company_website, address, email, description, phone, capacity) VALUES
(1, 6, 'TechCorp Solutions', 'https://www.techcorp.com', '100 Tech Boulevard, San Francisco, CA 94102', 'careers@techcorp.com', $$Leading software development company specializing in cloud solutions and AI-powered applications. We are committed to innovation and excellence.$$, '(415) 555-0121', 500);

INSERT INTO employer_profile (id, owner_id, company_name, company_website, address, email, description, phone, capacity) VALUES
(2, 7, 'Finance Plus Inc.', 'https://www.financeplus.com', '200 Finance Avenue, New York, NY 10020', 'hr@financeplus.com', $$Premier financial services firm offering investment banking, wealth management, and financial consulting. Join our team of experts.$$, '(212) 555-0122', 300);

-- ============================================================================
-- 6. INSERT RESUME DATA
-- ============================================================================
INSERT INTO resume (id, file_url, uploaded_at, default_resume, job_seeker_id) VALUES
(1, '/resumes/john_doe_resume.pdf', NOW(), true, 1),
(2, '/resumes/jane_smith_resume.pdf', NOW(), true, 2),
(3, '/resumes/michael_johnson_resume.pdf', NOW(), true, 3),
(4, '/resumes/sarah_williams_resume.pdf', NOW(), true, 4),
(5, '/resumes/john_doe_resume_v2.pdf', NOW(), false, 1);

-- ============================================================================
-- 7. INSERT JOB_POST DATA
-- ============================================================================
INSERT INTO job_post (id, employer_id, title, description, employment_type, status, salary_min, salary_max, created_at) VALUES
(1, 1, 'Senior Java Developer', 
 'We are looking for an experienced Java developer with 5+ years of experience in enterprise application development. You will work with our team on cloud-based solutions using Spring Boot and microservices architecture.', 
 'FULL_TIME', 'OPEN', 120000.00, 160000.00, NOW()),

(2, 1, 'Frontend Developer (React)', 
 'Join our frontend team and develop modern, responsive web applications using React and TypeScript. Strong UI/UX understanding required. Work in an Agile environment with a talented team.', 
 'FULL_TIME', 'OPEN', 100000.00, 140000.00, NOW()),

(3, 1, 'DevOps Engineer', 
 'Seeking a DevOps engineer to manage our cloud infrastructure on AWS. Experience with Docker, Kubernetes, and CI/CD pipelines is essential. Help us scale our platform.', 
 'FULL_TIME', 'OPEN', 130000.00, 170000.00, NOW()),

(4, 2, 'Financial Analyst', 
 'We need a detail-oriented financial analyst to support our investment team. Strong Excel skills, financial modeling, and analysis are required. MBA preferred.', 
 'FULL_TIME', 'OPEN', 80000.00, 120000.00, NOW()),

(5, 2, 'Risk Management Consultant', 
 'Join our risk management team and help clients identify and mitigate financial risks. Excellent communication and analytical skills required.', 
 'FULL_TIME', 'DFRAFT', 90000.00, 130000.00, NOW()),

(6, 1, 'QA Engineer (Part-Time)', 
 'Part-time QA engineer needed for automated testing. Experience with Selenium or similar frameworks required.', 
 'PART_TIME', 'OPEN', 30000.00, 50000.00, NOW());

-- ============================================================================
-- 8. INSERT JOB_INDUSTRY DATA
-- ============================================================================
-- Associate job posts with industries
INSERT INTO job_industry (id, industry_id, job_post_id) VALUES
(1, 1, 1),  -- Senior Java Developer - Information Technology
(2, 1, 2),  -- Frontend Developer - Information Technology
(3, 1, 3),  -- DevOps Engineer - Information Technology
(4, 1, 6),  -- QA Engineer - Information Technology
(5, 2, 4),  -- Financial Analyst - Finance
(6, 2, 5),  -- Risk Management Consultant - Finance
(7, 7, 4),  -- Financial Analyst - Marketing (cross-industry)
(8, 7, 5);  -- Risk Management Consultant - Marketing (cross-industry)

-- ============================================================================
-- 9. INSERT JOB_APPLICATION DATA
-- ============================================================================
INSERT INTO job_application (id, job_seeker_id, job_post_id, resume_id, cover_letter, status, applied_at) VALUES
(1, 1, 1, 1, 
 'I am very interested in the Senior Java Developer position. With 8 years of experience in enterprise Java development and expertise in Spring Boot, I believe I am a strong fit for your team.', 
 'PENDING', NOW()),

(2, 1, 3, 1, 
 'As a DevOps enthusiast with hands-on experience in AWS and Kubernetes, I am excited to apply for the DevOps Engineer role. I have successfully managed production infrastructure for multiple projects.', 
 'PENDING', NOW()),

(3, 2, 2, 2, 
 'I am a passionate React developer with 6 years of experience building modern web applications. I would love to contribute to your team and help create excellent user experiences.', 
 'REVIEWING', NOW()),

(4, 3, 4, 3, 
 'With my background in financial analysis and strong quantitative skills, I am confident I can contribute significantly to your investment team.', 
 'INTERVIEW', NOW()),

(5, 4, 2, 4, 
 'I have been working as a frontend developer for 5 years with expertise in React, TypeScript, and modern CSS frameworks. I am eager to join your innovative team.', 
 'PENDING', NOW()),

(6, 4, 6, 4, 
 'I am interested in the QA Engineer position and have experience with Selenium and automated testing frameworks.', 
 'PENDING', NOW());

-- ============================================================================
-- SEQUENCE RESET (for PostgreSQL auto-increment)
-- ============================================================================
-- Reset sequences to ensure new records start from appropriate IDs
SELECT setval('industry_id_seq', (SELECT MAX(id) FROM industry) + 1);
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users) + 1);
SELECT setval('users_role_id_seq', (SELECT MAX(id) FROM users_role) + 1);
SELECT setval('job_seeker_profile_id_seq', (SELECT MAX(id) FROM job_seeker_profile) + 1);
SELECT setval('employer_profile_id_seq', (SELECT MAX(id) FROM employer_profile) + 1);
SELECT setval('resume_id_seq', (SELECT MAX(id) FROM resume) + 1);
SELECT setval('job_post_id_seq', (SELECT MAX(id) FROM job_post) + 1);
SELECT setval('job_industry_id_seq', (SELECT MAX(id) FROM job_industry) + 1);
SELECT setval('job_application_id_seq', (SELECT MAX(id) FROM job_application) + 1);

-- ============================================================================
-- END OF SEED FILE
-- ============================================================================
-- Notes:
-- 1. All passwords are hashed using BCrypt with strength 10
-- 2. All timestamps use NOW() which will be set at insertion time
-- 3. Ensure that spring.jpa.hibernate.ddl-auto is set to "update" or "validate"
-- 4. For fresh database, use "create" for the first run, then switch to "update"
-- 5. All foreign key relationships are properly maintained
-- ============================================================================
