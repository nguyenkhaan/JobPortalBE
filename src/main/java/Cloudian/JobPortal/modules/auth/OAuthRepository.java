package Cloudian.JobPortal.modules.auth;

import Cloudian.JobPortal.models.OAuth;
import Cloudian.JobPortal.models.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuthRepository extends JpaRepository<OAuth , Long> {
    public Optional<OAuth> findByUserIdAndProvider(Long userId, Provider provider);
}
