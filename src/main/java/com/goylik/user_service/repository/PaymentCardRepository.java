package com.goylik.user_service.repository;

import com.goylik.user_service.model.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long> {
    List<PaymentCard> findByUserId(Long userId);
    long countByUserIdAndDeletedAtIsNull(Long userId);

    @Query("""
            SELECT c
            FROM PaymentCard c
            JOIN FETCH c.user
            WHERE c.id = :id
            """)
    Optional<PaymentCard> findByIdWithUser(@Param("id") Long id);

    @Query("""
            SELECT c
            FROM PaymentCard c
            WHERE c.user.id = :userId
            AND c.active = true
            """)
    List<PaymentCard> findActiveCardsByUser(@Param("userId") Long userId);

    @Modifying
    @Query(value = """
            UPDATE payment_cards c
            SET c.active = :active,
                c.updated_at = now()
            WHERE c.id = :id
            """, nativeQuery = true)
    void updateActiveStatus(@Param("id") Long id,
                            @Param("active") boolean active);

}
