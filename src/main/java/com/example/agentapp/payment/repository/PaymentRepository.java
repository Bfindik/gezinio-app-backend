package com.example.agentapp.payment.repository;

import com.example.agentapp.payment.model.Payment;
import com.example.agentapp.payment.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByReservationId(Long reservationId);

    List<Payment> findByReservationIdAndPaymentStatus(Long reservationId, PaymentStatus status);

    boolean existsByTransactionReference(String transactionReference);

    Optional<Payment> findByTransactionReference(String transactionReference);

    // Bir rezervasyonun tamamlanan ödemelerinin toplam tutarı (base currency)
    @Query("SELECT COALESCE(SUM(p.amountInBaseCurrency), 0) FROM Payment p " +
           "WHERE p.reservation.id = :reservationId AND p.paymentStatus = 'COMPLETED'")
    BigDecimal sumCompletedPaymentsByReservationId(@Param("reservationId") Long reservationId);
}