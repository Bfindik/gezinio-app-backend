package com.example.gezinio.payment.repository;

import com.example.gezinio.payment.model.Payment;
import com.example.gezinio.payment.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    // Tüm tamamlanan ödemelerin toplam geliri (base currency)
    @Query("SELECT COALESCE(SUM(p.amountInBaseCurrency), 0) FROM Payment p WHERE p.paymentStatus = 'COMPLETED'")
    BigDecimal sumAllCompletedPayments();

    // Belirli tarihten sonraki tamamlanan ödemelerin toplam geliri
    @Query("SELECT COALESCE(SUM(p.amountInBaseCurrency), 0) FROM Payment p " +
           "WHERE p.paymentStatus = 'COMPLETED' AND p.paymentDate >= :since")
    BigDecimal sumCompletedPaymentsSince(@Param("since") LocalDateTime since);

    // Bir turun tüm rezervasyonlarındaki tamamlanan ödemelerin toplamı
    @Query("SELECT COALESCE(SUM(p.amountInBaseCurrency), 0) FROM Payment p " +
           "WHERE p.paymentStatus = 'COMPLETED' AND p.reservation.tour.id = :tourId")
    BigDecimal sumCompletedPaymentsByTourId(@Param("tourId") Long tourId);
}