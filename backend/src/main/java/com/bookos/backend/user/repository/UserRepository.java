package com.bookos.backend.user.repository;

import com.bookos.backend.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"role", "profile"})
    Optional<User> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = {"role", "profile"})
    List<User> findAllByOrderByCreatedAtAsc();

    boolean existsByEmailIgnoreCase(String email);
}
