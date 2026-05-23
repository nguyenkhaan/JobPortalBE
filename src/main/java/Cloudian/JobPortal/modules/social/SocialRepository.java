package Cloudian.JobPortal.modules.social;

import Cloudian.JobPortal.models.Social;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SocialRepository extends JpaRepository<Social, Long> {
}
