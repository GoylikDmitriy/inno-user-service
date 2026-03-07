package com.goylik.user_service.repository;

import com.goylik.user_service.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("""
            SELECT u
            FROM User u
            WHERE u.active = true
            """)
    Page<User> findAllActiveUsers(Pageable pageable);

    @Modifying
    @Query(value = """
            UPDATE users u
            SET u.active = :active,
                u.updated_at = now()
            WHERE u.id = :id
            """, nativeQuery = true)
    void updateActiveStatus(@Param("id") Long id,
                            @Param("active") boolean active);
}