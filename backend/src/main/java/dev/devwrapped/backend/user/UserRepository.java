package dev.devwrapped.backend.user;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByGithubId(Long githubId);

    Optional<User> findByGithubLogin(String githubLogin);

    List<User> findTop3ByOrderByIdAsc();
}
