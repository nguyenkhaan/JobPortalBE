package Cloudian.JobPortal.modules.interview;

import Cloudian.JobPortal.models.InterviewSession;
import Cloudian.JobPortal.models.InterviewSessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {
    boolean existsByApplication_IdAndStatusIn(Long applicationId, List<InterviewSessionStatus> statuses);

    @Query("""
            SELECT s FROM InterviewSession s
            JOIN s.application a
            JOIN a.jobPost jp
            JOIN jp.employer e
            WHERE e.owner.id = :userId
            AND (:status IS NULL OR s.status = :status)
            ORDER BY s.createdAt DESC
            """)
    Page<InterviewSession> findForEmployer(
            @Param("userId") Long userId,
            @Param("status") InterviewSessionStatus status,
            Pageable pageable
    );

    @Query("""
            SELECT s FROM InterviewSession s
            JOIN s.application a
            JOIN a.jobSeeker js
            WHERE js.user.id = :userId
            AND (:status IS NULL OR s.status = :status)
            ORDER BY s.createdAt DESC
            """)
    Page<InterviewSession> findForSeeker(
            @Param("userId") Long userId,
            @Param("status") InterviewSessionStatus status,
            Pageable pageable
    );

    @Query("""
            SELECT s FROM InterviewSession s
            WHERE s.application.id = :applicationId
            ORDER BY s.createdAt DESC
            """)
    List<InterviewSession> findByApplicationIdOrderByCreatedAtDesc(@Param("applicationId") Long applicationId);

    List<InterviewSession> findByStatusAndExpiresAtBefore(InterviewSessionStatus status, LocalDateTime now);
}
