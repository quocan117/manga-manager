package com.example.backend.repository;

import com.example.backend.model.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.userId = :userId")
    Optional<User> findByIdForUpdate(@Param("userId") Long userId);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    List<User> findByUsernameContainingIgnoreCase(String username);

    List<User> findAllByOrderByCreatedAtDesc();

    long countByRoleRoleNameAndStatus(String roleName, String status);

    List<User> findByRoleRoleNameAndStatusOrderByUsernameAsc(String roleName, String status);

    @Query("""
            select u from User u
            where upper(u.role.roleName) = 'TANTOU_EDITOR'
              and upper(u.status) = 'ACTIVE'
              and u.specialty is not null
            order by u.username asc
            """)
    List<User> findActiveTantouEditorsWithSpecialtyOrderByUsernameAsc();

    List<User> findByRoleRoleNameAndCreatedByOrderByUsernameAsc(String roleName, User createdBy);
}
