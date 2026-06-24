package com.example.backend.repository;

import com.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    List<User> findByUsernameContainingIgnoreCase(String username);

    List<User> findAllByOrderByCreatedAtDesc();

    long countByRoleRoleNameAndStatus(String roleName, String status);

    List<User> findByRoleRoleNameAndStatusOrderByUsernameAsc(String roleName, String status);
}
