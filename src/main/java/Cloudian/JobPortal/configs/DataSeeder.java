// Running seeder (stop any app already on 8080 first, or use another port):
//   .\gradlew bootRun --args="--seeder --server.port=8081"
package Cloudian.JobPortal.configs;

import Cloudian.JobPortal.models.*;
import Cloudian.JobPortal.modules.employer.EmployerRepository;
import Cloudian.JobPortal.modules.industry.IndustryRepository;
import Cloudian.JobPortal.modules.jobapplication.JobApplicationRepository;
import Cloudian.JobPortal.modules.jobindustry.JobIndustryRepository;
import Cloudian.JobPortal.modules.jobpost.JobPostRepository;
import Cloudian.JobPortal.modules.jobseeker.JobSeekerRepository;
import Cloudian.JobPortal.modules.payment.PaymentRepository;
import Cloudian.JobPortal.modules.resume.ResumeRepository;
import Cloudian.JobPortal.modules.role.UserRoleRepository;
import Cloudian.JobPortal.modules.social.SocialRepository;
import Cloudian.JobPortal.modules.user.UserRepository;
import Cloudian.JobPortal.modules.auth.OAuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private static final String DEFAULT_PASSWORD = "123456";
    private static final int SEED_COUNT = 6;

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final IndustryRepository industryRepository;
    private final EmployerRepository employerRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final ResumeRepository resumeRepository;
    private final JobPostRepository jobPostRepository;
    private final JobIndustryRepository jobIndustryRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final PaymentRepository paymentRepository;
    private final SocialRepository socialRepository;
    private final OAuthRepository oAuthRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (!args.containsOption("seeder")) {
            return;
        }
        log.info("DataSeeder: --seeder flag detected, starting seed...");

        if (jobPostRepository.count() > 0
                && resumeRepository.count() > 0
                && jobApplicationRepository.count() > 0) {
            log.info("DataSeeder: job posts, resumes, and applications already exist — skipping.");
            return;
        }

        String hashedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);

        List<User> users;
        if (userRepository.count() == 0) {
            users = userRepository.saveAll(seedUsers(hashedPassword));
            userRoleRepository.saveAll(seedUserRoles(users));
        } else {
            users = userRepository.findAll();
        }

        List<Industry> industries = industryRepository.count() == 0
                ? industryRepository.saveAll(seedIndustries())
                : industryRepository.findAll();

        List<EmployerProfile> employers = employerRepository.count() == 0
                ? employerRepository.saveAll(seedEmployers(users))
                : employerRepository.findAll();

        List<JobSeekerProfile> seekers = jobSeekerRepository.count() == 0
                ? jobSeekerRepository.saveAll(seedJobSeekers(users))
                : jobSeekerRepository.findAll();

        List<Resume> resumes = resumeRepository.count() == 0
                ? resumeRepository.saveAll(seedResumes(seekers))
                : resumeRepository.findAll();

        List<JobPost> jobPosts = jobPostRepository.count() == 0
                ? jobPostRepository.saveAll(seedJobPosts(employers))
                : jobPostRepository.findAll();

        if (jobIndustryRepository.count() == 0 && !jobPosts.isEmpty() && !industries.isEmpty()) {
            jobIndustryRepository.saveAll(seedJobIndustries(industries, jobPosts));
        }

        if (jobApplicationRepository.count() == 0 && !seekers.isEmpty() && !jobPosts.isEmpty() && !resumes.isEmpty()) {
            jobApplicationRepository.saveAll(seedJobApplications(seekers, jobPosts, resumes));
        }

        if (paymentRepository.count() == 0) {
            paymentRepository.saveAll(seedPayments(users));
        }

        if (socialRepository.count() == 0) {
            socialRepository.saveAll(seedSocials(users));
        }

        if (oAuthRepository.count() == 0) {
            oAuthRepository.saveAll(seedOAuths(users));
        }

        log.info("DataSeeder: finished — users={}, jobPosts={}, resumes={}, applications={}",
                userRepository.count(),
                jobPostRepository.count(),
                resumeRepository.count(),
                jobApplicationRepository.count());
    }

    private List<User> seedUsers(String hashedPassword) {
        List<User> users = new ArrayList<>();
        users.add(User.builder().email("seeker1@jobportal.test").password(hashedPassword).active(true).build());
        users.add(User.builder().email("seeker2@jobportal.test").password(hashedPassword).active(true).build());
        users.add(User.builder().email("seeker3@jobportal.test").password(hashedPassword).active(true).build());
        users.add(User.builder().email("seeker4@jobportal.test").password(hashedPassword).active(true).build());
        users.add(User.builder().email("seeker5@jobportal.test").password(hashedPassword).active(true).build());
        users.add(User.builder().email("seeker6@jobportal.test").password(hashedPassword).active(true).build());
        users.add(User.builder().email("employer1@jobportal.test").password(hashedPassword).active(true).build());
        users.add(User.builder().email("employer2@jobportal.test").password(hashedPassword).active(true).build());
        users.add(User.builder().email("employer3@jobportal.test").password(hashedPassword).active(true).build());
        users.add(User.builder().email("employer4@jobportal.test").password(hashedPassword).active(true).build());
        users.add(User.builder().email("employer5@jobportal.test").password(hashedPassword).active(true).build());
        users.add(User.builder().email("employer6@jobportal.test").password(hashedPassword).active(true).build());
        users.add(User.builder().email("admin@jobportal.test").password(hashedPassword).active(true).build());
        return users;
    }

    private List<UserRole> seedUserRoles(List<User> users) {
        List<UserRole> roles = new ArrayList<>();
        for (int i = 0; i < SEED_COUNT; i++) {
            roles.add(UserRole.builder().user(users.get(i)).role(Role.SEEKER).build());
        }
        for (int i = 0; i < SEED_COUNT; i++) {
            roles.add(UserRole.builder().user(users.get(SEED_COUNT + i)).role(Role.EMPLOYER).build());
        }
        User admin = users.get(12);
        roles.add(UserRole.builder().user(admin).role(Role.ADMIN).build());
        roles.add(UserRole.builder().user(admin).role(Role.SEEKER).build());
        roles.add(UserRole.builder().user(admin).role(Role.EMPLOYER).build());
        return roles;
    }

    private List<Industry> seedIndustries() {
        List<Industry> industries = new ArrayList<>();
        industries.add(Industry.builder().name("Information Technology").build());
        industries.add(Industry.builder().name("Finance & Banking").build());
        industries.add(Industry.builder().name("Healthcare").build());
        industries.add(Industry.builder().name("Education").build());
        industries.add(Industry.builder().name("Retail & E-Commerce").build());
        industries.add(Industry.builder().name("Manufacturing").build());
        return industries;
    }

    private List<EmployerProfile> seedEmployers(List<User> users) {
        String[] companies = {
                "Cloudian Tech", "Nova Solutions", "GreenField Labs",
                "Skyline Media", "Peak Logistics", "Bright Health Co."
        };
        String[] websites = {
                "https://cloudian.tech", "https://nova-solutions.io", "https://greenfieldlabs.com",
                "https://skylinemedia.vn", "https://peaklogistics.com", "https://brighthealth.co"
        };
        String[] addresses = {
                "123 Nguyen Hue, District 1, HCMC",
                "45 Le Loi, Hai Chau, Da Nang",
                "78 Tran Hung Dao, Hoan Kiem, Hanoi",
                "12 Vo Van Tan, District 3, HCMC",
                "56 Bach Dang, Hai Ba Trung, Hanoi",
                "90 Ly Thuong Kiet, District 10, HCMC"
        };

        List<EmployerProfile> employers = new ArrayList<>();
        for (int i = 0; i < SEED_COUNT; i++) {
            User owner = users.get(SEED_COUNT + i);
            employers.add(EmployerProfile.builder()
                    .owner(owner)
                    .companyName(companies[i])
                    .companyWebsite(websites[i])
                    .address(addresses[i])
                    .email(owner.getEmail())
                    .description("Sample employer profile for " + companies[i])
                    .phone("090000000" + (i + 1))
                    .capacity(50 + i * 10)
                    .active(true)
                    .build());
        }
        return employers;
    }

    private List<JobSeekerProfile> seedJobSeekers(List<User> users) {
        String[] names = {
                "Nguyen Van An", "Tran Thi Binh", "Le Hoang Cuong",
                "Pham Thi Dung", "Hoang Van Em", "Vo Thi Phuong"
        };
        String[] phones = {
                "0911111111", "0922222222", "0933333333",
                "0944444444", "0955555555", "0966666666"
        };
        String[] addresses = {
                "District 1, Ho Chi Minh City",
                "Hai Chau, Da Nang",
                "Hoan Kiem, Hanoi",
                "District 7, Ho Chi Minh City",
                "Nha Trang, Khanh Hoa",
                "Can Tho City"
        };
        Boolean[] approved = {
                true, false, false, true, false, true
        };
        List<JobSeekerProfile> seekers = new ArrayList<>();
        for (int i = 0; i < SEED_COUNT; i++) {
            seekers.add(JobSeekerProfile.builder()
                    .user(users.get(i))
                    .approve(approved[i])
                    .fullName(names[i])
                    .phone(phones[i])
                    .address(addresses[i])
                    .build());
        }
        return seekers;
    }

    private List<Resume> seedResumes(List<JobSeekerProfile> seekers) {
        List<Resume> resumes = new ArrayList<>();
        for (int i = 0; i < SEED_COUNT; i++) {
            resumes.add(Resume.builder()
                    .jobSeeker(seekers.get(i))
                    .fileUrl("https://storage.jobportal.test/resumes/resume-" + (i + 1) + ".pdf")
                    .isDefault(true)
                    .build());
        }
        return resumes;
    }

    private List<JobPost> seedJobPosts(List<EmployerProfile> employers) {
        EmploymentType[] types = {
                EmploymentType.FULL_TIME, EmploymentType.FULL_TIME, EmploymentType.PART_TIME,
                EmploymentType.FULL_TIME, EmploymentType.PART_TIME, EmploymentType.FULL_TIME
        };
        JobPostStatus[] statuses = {
                JobPostStatus.OPEN, JobPostStatus.OPEN, JobPostStatus.OPEN,
                JobPostStatus.OPEN, JobPostStatus.CLOSE, JobPostStatus.DFRAFT
        };
        EducationLevel[] educationLevels = {
                EducationLevel.BACHELOR, EducationLevel.BACHELOR, EducationLevel.ASSOCIATE,
                EducationLevel.MASTER, EducationLevel.BACHELOR, EducationLevel.HIGH_SCHOOL
        };
        JobLevel[] jobLevels = {
                JobLevel.JUNIOR, JobLevel.FRESHER, JobLevel.INTERN,
                JobLevel.MIDDLE, JobLevel.SENIOR, JobLevel.FRESHER
        };
        String[] titles = {
                "Backend Java Developer", "Frontend React Developer", "QA Engineer Intern",
                "DevOps Engineer", "Product Manager", "Customer Support Specialist"
        };
        String[] descriptions = {
                "Build REST APIs with Spring Boot and PostgreSQL.",
                "Develop responsive UI with React and TypeScript.",
                "Write test cases and automate regression testing.",
                "Maintain CI/CD pipelines on AWS and Docker.",
                "Own product roadmap for B2B hiring features.",
                "Support employers and job seekers via chat and email."
        };
        String[] tags = {
                "fullstack;backend", "fulltime;intern", "health;salary",
                "multi;union", "frontend;design", "support;customer"
        };
        LocalDateTime[] expiresAt = {
                LocalDateTime.of(2026, 6, 1, 0, 0),
                LocalDateTime.of(2026, 8, 1, 0, 0),
                LocalDateTime.of(2026, 6, 15, 12, 0),
                LocalDateTime.of(2026, 9, 1, 0, 0),
                LocalDateTime.of(2026, 7, 1, 18, 30),
                LocalDateTime.of(2026, 10, 1, 0, 0)
        };
        List<JobPost> posts = new ArrayList<>();
        for (int i = 0; i < SEED_COUNT; i++) {
            posts.add(JobPost.builder()
                    .employer(employers.get(i))
                    .title(titles[i])
                    .description(descriptions[i])
                    .employmentType(types[i])
                    .status(statuses[i])
                    .educationLevel(educationLevels[i])
                    .experience(i)
                    .jobLevel(jobLevels[i])
                    .expiresAt(expiresAt[i])
                    .tags(tags[i])
                    .salaryMin(BigDecimal.valueOf(8_000_000L + i * 1_000_000L))
                    .salaryMax(BigDecimal.valueOf(15_000_000L + i * 2_000_000L))
                    .build());
        }
        return posts;
    }

    private List<JobIndustry> seedJobIndustries(List<Industry> industries, List<JobPost> jobPosts) {
        List<JobIndustry> links = new ArrayList<>();
        for (int i = 0; i < SEED_COUNT; i++) {
            links.add(JobIndustry.builder()
                    .industry(industries.get(i))
                    .jobPost(jobPosts.get(i))
                    .build());
        }
        return links;
    }

    private List<JobApplication> seedJobApplications(
            List<JobSeekerProfile> seekers,
            List<JobPost> jobPosts,
            List<Resume> resumes
    ) {
        JobApplicationStatus[] statuses = {
                JobApplicationStatus.PENDING,
                JobApplicationStatus.REVIEWING,
                JobApplicationStatus.INTERVIEW,
                JobApplicationStatus.OFFER,
                JobApplicationStatus.ACCEPTED,
                JobApplicationStatus.REJECTED
        };
        String[] coverLetters = {
                "I am excited to apply for this backend role.",
                "My React experience matches your frontend stack.",
                "I want to grow as a QA engineer in your team.",
                "I have hands-on experience with Docker and Jenkins.",
                "I led two product launches in my previous company.",
                "I enjoy helping users and resolving issues quickly."
        };
        List<JobApplication> applications = new ArrayList<>();
        for (int i = 0; i < SEED_COUNT; i++) {
            applications.add(JobApplication.builder()
                    .jobSeeker(seekers.get(i))
                    .jobPost(jobPosts.get(i))
                    .resume(resumes.get(i))
                    .coverLetter(coverLetters[i])
                    .status(statuses[i])
                    .build());
        }
        return applications;
    }

    private List<Payment> seedPayments(List<User> users) {
        PaymentMethod[] methods = {
                PaymentMethod.MOMO, PaymentMethod.ZALO_PAY, PaymentMethod.CASH,
                PaymentMethod.MOMO, PaymentMethod.ZALO_PAY, PaymentMethod.CASH
        };
        PaymentStatus[] statuses = {
                PaymentStatus.AGREE, PaymentStatus.PENDING, PaymentStatus.REJECT,
                PaymentStatus.AGREE, PaymentStatus.PENDING, PaymentStatus.AGREE
        };
        Double[] costs = {199_000.0, 299_000.0, 99_000.0, 499_000.0, 149_000.0, 249_000.0};

        List<Payment> payments = new ArrayList<>();
        for (int i = 0; i < SEED_COUNT; i++) {
            payments.add(Payment.builder()
                    .user(users.get(i))
                    .cost(costs[i])
                    .method(methods[i])
                    .status(statuses[i])
                    .note("Sample payment #" + (i + 1))
                    .build());
        }
        return payments;
    }

    private List<Social> seedSocials(List<User> users) {
        String[] titles = {"LinkedIn", "GitHub", "Facebook", "Twitter", "Portfolio", "YouTube"};
        String[] links = {
                "https://linkedin.com/in/seeker1",
                "https://github.com/seeker2",
                "https://facebook.com/seeker3",
                "https://twitter.com/seeker4",
                "https://portfolio.jobportal.test/seeker5",
                "https://youtube.com/@seeker6"
        };

        List<Social> socials = new ArrayList<>();
        for (int i = 0; i < SEED_COUNT; i++) {
            socials.add(Social.builder()
                    .user(users.get(i))
                    .title(titles[i])
                    .link(links[i])
                    .build());
        }
        return socials;
    }

    private List<OAuth> seedOAuths(List<User> users) {
        Provider[] providers = {
                Provider.GOOGLE, Provider.GOOGLE, Provider.LOCAL,
                Provider.GOOGLE, Provider.LOCAL, Provider.GOOGLE
        };

        List<OAuth> oAuths = new ArrayList<>();
        for (int i = 0; i < SEED_COUNT; i++) {
            oAuths.add(OAuth.builder()
                    .user(users.get(i))
                    .provider(providers[i])
                    .providerId("oauth-provider-id-" + (i + 1))
                    .refreshToken("oauth-refresh-token-" + (i + 1))
                    .build());
        }
        return oAuths;
    }
}
