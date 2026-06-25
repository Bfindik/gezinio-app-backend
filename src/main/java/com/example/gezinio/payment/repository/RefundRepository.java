package com.example.gezinio.payment.repository;

import com.example.gezinio.payment.model.Refund;
import com.example.gezinio.payment.model.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    List<Refund> findByPaymentId(Long paymentId);

    List<Refund> findByPaymentIdAndRefundStatus(Long paymentId, RefundStatus status);

    // Bir rezervasyona ait tüm tamamlanan geri ödemelerin toplamı
    @Query("SELECT COALESCE(SUM(r.refundAmountInBaseCurrency), 0) FROM Refund r " +
           "WHERE r.payment.reservation.id = :reservationId AND r.refundStatus = 'COMPLETED'")
    BigDecimal sumCompletedRefundsByReservationId(@Param("reservationId") Long reservationId);

    // Tüm tamamlanan iadelerin toplamı
    @Query("SELECT COALESCE(SUM(r.refundAmountInBaseCurrency), 0) FROM Refund r WHERE r.refundStatus = 'COMPLETED'")
    BigDecimal sumAllCompletedRefunds();
}