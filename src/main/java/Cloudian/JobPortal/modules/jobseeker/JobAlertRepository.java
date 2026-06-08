package Cloudian.JobPortal.modules.jobseeker;

import Cloudian.JobPortal.models.JobAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobAlertRepository extends JpaRepository<JobAlert, Long> {

    @Query("SELECT ja FROM JobAlert ja WHERE ja.jobSeeker.user.id = :userId ORDER BY ja.createdAt DESC")
    Page<JobAlert> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT ja FROM JobAlert ja WHERE ja.jobSeeker.user.id = :userId ORDER BY ja.createdAt DESC")
    List<JobAlert> findByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(ja) FROM JobAlert ja WHERE ja.jobSeeker.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT ja FROM JobAlert ja WHERE ja.id = :id AND ja.jobSeeker.user.id = :userId")
    Optional<JobAlert> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}