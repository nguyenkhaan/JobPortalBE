package Cloudian.JobPortal.modules.token;

import Cloudian.JobPortal.models.Token;
import Cloudian.JobPortal.models.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token , Long>
{
    Optional<Token> getByUsersIDAndType(Long userID , TokenType type);
    Optional<Token> getByToken(String token);
}
