package Cloudian.JobPortal.modules.interview;

import Cloudian.JobPortal.models.InterviewSchedule;
import Cloudian.JobPortal.models.InterviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewScheduleRepository extends JpaRepository<InterviewSchedule, Long> {
    Optional<InterviewSchedule> findByJobApplicationId(Long jobApplicationId);

    @Query("SELECT isc FROM InterviewSchedule isc " +
           "LEFT JOIN FETCH isc.slots " +
           "LEFT JOIN FETCH isc.jobApplication ja " +
           "LEFT JOIN FETCH ja.jobPost jp " +
           "WHERE isc.id = :id")
    Optional<InterviewSchedule> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT isc FROM InterviewSchedule isc " +
           "LEFT JOIN FETCH isc.slots " +
           "LEFT JOIN FETCH isc.jobApplication ja " +
           "LEFT JOIN FETCH ja.jobPost jp " +
           "WHERE isc.jobApplication.id = :applicationId")
    Optional<InterviewSchedule> findByJobApplicationIdWithDetails(@Param("applicationId") Long applicationId);

    @Query("SELECT isc FROM InterviewSchedule isc " +
           "JOIN FETCH isc.jobApplication ja " +
           "WHERE isc.status = :status AND isc.createdAt < :deadline")
    List<InterviewSchedule> findByStatusAndCreatedAtBefore(
            @Param("status") InterviewStatus status,
            @Param("deadline") LocalDateTime deadline);

    @Query("SELECT isc FROM InterviewSchedule isc " +
           "LEFT JOIN FETCH isc.slots " +
           "LEFT JOIN FETCH isc.jobApplication ja " +
           "LEFT JOIN FETCH ja.jobPost jp " +
           "WHERE ja.jobPost.employer.owner.id = :userId " +
           "ORDER BY isc.createdAt DESC")
    List<InterviewSchedule> findByEmployerUserId(@Param("userId") Long userId);

    @Query("SELECT isc FROM InterviewSchedule isc " +
           "LEFT JOIN FETCH isc.slots " +
           "LEFT JOIN FETCH isc.jobApplication ja " +
           "LEFT JOIN FETCH ja.jobPost jp " +
           "WHERE ja.jobSeeker.user.id = :userId " +
           "ORDER BY isc.createdAt DESC")
    List<InterviewSchedule> findBySeekerUserId(@Param("userId") Long userId);

    boolean existsByJobApplicationId(Long jobApplicationId);
}