package com.example.gezinio.payment.service;

import com.example.gezinio.payment.dto.*;
import com.example.gezinio.payment.model.*;
import com.example.gezinio.payment.repository.PaymentRepository;
import com.example.gezinio.payment.repository.RefundRepository;
import com.example.gezinio.reservation.model.Reservation;
import com.example.gezinio.reservation.model.ReservationStatus;
import com.example.gezinio.reservation.repository.ReservationRepository;
import com.example.gezinio.notification.event.AppNotificationEvent;
import com.example.gezinio.notification.model.NotificationEventType;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final ReservationRepository reservationRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository,
                          RefundRepository refundRepository,
                          ReservationRepository reservationRepository,
                          ApplicationEventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.refundRepository = refundRepository;
        this.reservationRepository = reservationRepository;
        this.eventPublisher = eventPublisher;
    }

    // ─── Ödeme ───────────────────────────────────────────────────────────────

    @Transactional
    public PaymentDTO createPayment(PaymentCreateRequest request) {
        Reservation reservation = findReservation(request.getReservationId());

        if (reservation.getReservationStatus() == ReservationStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot pay for a cancelled reservation");
        }
        if (reservation.getReservationStatus() == ReservationStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation is already completed");
        }

        // Tekil transaction reference kontrolü
        if (request.getTransactionReference() != null &&
                paymentRepository.existsByTransactionReference(request.getTransactionReference())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Transaction reference already exists");
        }

        // Exchange rate ile base currency'ye çevir
        BigDecimal exchangeRate = request.getExchangeRate() != null ? request.getExchangeRate() : BigDecimal.ONE;
        BigDecimal amountInBase = request.getAmount().multiply(exchangeRate);

        // Zaten ödenen tutarı kontrol et — fazla ödeme engeli
        BigDecimal alreadyPaid = paymentRepository.sumCompletedPaymentsByReservationId(reservation.getId());
        BigDecimal remaining = reservation.getTotalPrice().subtract(alreadyPaid);

        if (amountInBase.compareTo(remaining) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Payment amount (%.2f %s) exceeds remaining balance (%.2f %s)",
                            amountInBase, reservation.getCurrency(),
                            remaining, reservation.getCurrency()));
        }

        Payment payment = new Payment();
        payment.setReservation(reservation);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency().toUpperCase());
        payment.setExchangeRate(exchangeRate);
        payment.setAmountInBaseCurrency(amountInBase);
        payment.setTransactionReference(request.getTransactionReference());
        payment.setCardLastFour(request.getCardLastFour());
        payment.setCardHolderName(request.getCardHolderName());
        payment.setBankName(request.getBankName());
        payment.setSenderAccountMasked(request.getSenderAccountMasked());
        payment.setPaymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDateTime.now());
        payment.setNotes(request.getNotes());

        Payment saved = paymentRepository.save(payment);
        logger.info("Payment created: {} for reservation: {}", saved.getId(), reservation.getId());

        // Rezervasyon ödeme durumunu güncelle
        updateReservationPaymentStatus(reservation);

        // Ödeme bildirimi gönder
        com.example.gezinio.auth.model.User user = reservation.getUser();
        eventPublisher.publishEvent(new AppNotificationEvent(
                this, NotificationEventType.PAYMENT_RECEIVED,
                user.getId(), user.getUsername(), user.getEmail(), user.getPhone()
        ).withReservation(reservation.getId(),
                reservation.getTour() != null ? reservation.getTour().getName() : null,
                reservation.getTour() != null ? reservation.getTour().getDestination() : null,
                reservation.getTotalPrice(), reservation.getCurrency())
         .withPayment(saved.getAmountInBaseCurrency(), reservation.getCurrency()));

        return toDTO(saved);
    }

    public PaymentDTO getPaymentById(Long id) {
        return toDTO(findPayment(id));
    }

    public List<PaymentDTO> getPaymentsByReservation(Long reservationId) {
        findReservation(reservationId);
        return paymentRepository.findByReservationId(reservationId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ReservationPaymentSummaryDTO getPaymentSummary(Long reservationId) {
        Reservation reservation = findReservation(reservationId);
        List<Payment> payments = paymentRepository.findByReservationId(reservationId);

        BigDecimal totalPaid = paymentRepository.sumCompletedPaymentsByReservationId(reservationId);
        BigDecimal totalRefunded = refundRepository.sumCompletedRefundsByReservationId(reservationId);
        BigDecimal remaining = reservation.getTotalPrice().subtract(totalPaid).add(totalRefunded);

        ReservationPaymentSummaryDTO summary = new ReservationPaymentSummaryDTO();
        summary.setReservationId(reservationId);
        summary.setTotalPrice(reservation.getTotalPrice());
        summary.setBaseCurrency(reservation.getCurrency());
        summary.setTotalPaid(totalPaid);
        summary.setTotalRefunded(totalRefunded);
        summary.setRemainingBalance(remaining.max(BigDecimal.ZERO));
        summary.setPaymentStatus(reservation.getPaymentStatus());
        summary.setPayments(payments.stream().map(this::toDTO).collect(Collectors.toList()));

        return summary;
    }

    // ─── Geri Ödeme ──────────────────────────────────────────────────────────

    @Transactional
    public RefundDTO createRefund(RefundCreateRequest request, Long processedBy) {
        Payment payment = findPayment(request.getPaymentId());

        if (payment.getPaymentStatus() == PaymentStatus.FAILED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot refund a failed payment");
        }

        // Zaten iade edilen tutar
        BigDecimal alreadyRefunded = payment.getTotalRefundedAmount();
        // İade edilebilecek max tutar (iptal ücreti düşüldükten sonra)
        BigDecimal cancellationFee = request.getCancellationFee() != null ? request.getCancellationFee() : BigDecimal.ZERO;
        BigDecimal maxRefundable = payment.getAmountInBaseCurrency().subtract(alreadyRefunded).subtract(cancellationFee);

        if (maxRefundable.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No refundable amount remaining for this payment");
        }

        // Geri ödeme tutarını base currency'e çevir (ödemenin kuru kullanılır)
        BigDecimal refundAmountInBase = request.getRefundAmount().multiply(payment.getExchangeRate());

        if (refundAmountInBase.compareTo(maxRefundable) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Refund amount (%.2f) exceeds max refundable amount (%.2f) after cancellation fee",
                            refundAmountInBase, maxRefundable));
        }

        Refund refund = new Refund();
        refund.setPayment(payment);
        refund.setRefundAmount(request.getRefundAmount());
        refund.setRefundCurrency(request.getRefundCurrency().toUpperCase());
        refund.setRefundAmountInBaseCurrency(refundAmountInBase);
        refund.setCancellationFee(cancellationFee);
        refund.setRefundReason(request.getRefundReason());
        refund.setRefundStatus(RefundStatus.COMPLETED);
        refund.setTransactionReference(request.getTransactionReference());
        refund.setRefundDate(LocalDateTime.now());
        refund.setProcessedBy(processedBy);
        refund.setNotes(request.getNotes());

        Refund saved = refundRepository.save(refund);
        logger.info("Refund created: {} for payment: {}", saved.getId(), payment.getId());

        // Ödeme ve rezervasyon durumlarını güncelle
        updatePaymentRefundStatus(payment);
        Reservation refundReservation = payment.getReservation();
        updateReservationPaymentStatus(refundReservation);

        // İade bildirimi gönder
        com.example.gezinio.auth.model.User refundUser = refundReservation.getUser();
        eventPublisher.publishEvent(new AppNotificationEvent(
                this, NotificationEventType.REFUND_PROCESSED,
                refundUser.getId(), refundUser.getUsername(), refundUser.getEmail(), refundUser.getPhone()
        ).withReservation(refundReservation.getId(),
                refundReservation.getTour() != null ? refundReservation.getTour().getName() : null,
                refundReservation.getTour() != null ? refundReservation.getTour().getDestination() : null,
                refundReservation.getTotalPrice(), refundReservation.getCurrency())
         .withRefund(saved.getRefundAmountInBaseCurrency()));

        return toRefundDTO(saved);
    }

    public RefundDTO getRefundById(Long id) {
        Refund refund = refundRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Refund not found: " + id));
        return toRefundDTO(refund);
    }

    public List<RefundDTO> getRefundsByPayment(Long paymentId) {
        findPayment(paymentId);
        return refundRepository.findByPaymentId(paymentId).stream()
                .map(this::toRefundDTO)
                .collect(Collectors.toList());
    }

    // ─── Durum güncelleme ────────────────────────────────────────────────────

    private void updateReservationPaymentStatus(Reservation reservation) {
        BigDecimal totalPaid = paymentRepository.sumCompletedPaymentsByReservationId(reservation.getId());
        BigDecimal totalRefunded = refundRepository.sumCompletedRefundsByReservationId(reservation.getId());

        com.example.gezinio.reservation.model.PaymentStatus newPaymentStatus;

        if (totalPaid.compareTo(BigDecimal.ZERO) == 0) {
            newPaymentStatus = com.example.gezinio.reservation.model.PaymentStatus.UNPAID;
        } else if (totalRefunded.compareTo(BigDecimal.ZERO) > 0 && totalRefunded.compareTo(totalPaid) >= 0) {
            newPaymentStatus = com.example.gezinio.reservation.model.PaymentStatus.REFUNDED;
        } else if (totalRefunded.compareTo(BigDecimal.ZERO) > 0) {
            newPaymentStatus = com.example.gezinio.reservation.model.PaymentStatus.PARTIALLY_REFUNDED;
        } else if (totalPaid.compareTo(reservation.getTotalPrice()) >= 0) {
            newPaymentStatus = com.example.gezinio.reservation.model.PaymentStatus.PAID;
            // Tam ödeme yapıldıysa rezervasyonu onayla
            if (reservation.getReservationStatus() == ReservationStatus.PENDING) {
                reservation.setReservationStatus(ReservationStatus.CONFIRMED);
                logger.info("Reservation {} auto-confirmed after full payment", reservation.getId());
            }
        } else {
            newPaymentStatus = com.example.gezinio.reservation.model.PaymentStatus.PARTIALLY_PAID;
        }

        reservation.setPaymentStatus(newPaymentStatus);
        reservationRepository.save(reservation);
    }

    private void updatePaymentRefundStatus(Payment payment) {
        BigDecimal totalRefunded = payment.getTotalRefundedAmount();

        if (totalRefunded.compareTo(payment.getAmountInBaseCurrency()) >= 0) {
            payment.setPaymentStatus(PaymentStatus.REFUNDED);
        } else if (totalRefunded.compareTo(BigDecimal.ZERO) > 0) {
            payment.setPaymentStatus(PaymentStatus.PARTIALLY_REFUNDED);
        }
        paymentRepository.save(payment);
    }

    // ─── Yardımcılar ─────────────────────────────────────────────────────────

    private Reservation findReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found: " + id));
    }

    private Payment findPayment(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found: " + id));
    }

    // ─── DTO Mapping ─────────────────────────────────────────────────────────

    public PaymentDTO toDTO(Payment p) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(p.getId());
        dto.setReservationId(p.getReservation().getId());
        dto.setPaymentMethod(p.getPaymentMethod());
        dto.setPaymentStatus(p.getPaymentStatus());
        dto.setAmount(p.getAmount());
        dto.setCurrency(p.getCurrency());
        dto.setExchangeRate(p.getExchangeRate());
        dto.setAmountInBaseCurrency(p.getAmountInBaseCurrency());
        dto.setTransactionReference(p.getTransactionReference());
        dto.setCardLastFour(p.getCardLastFour());
        dto.setCardHolderName(p.getCardHolderName());
        dto.setBankName(p.getBankName());
        dto.setSenderAccountMasked(p.getSenderAccountMasked());
        dto.setPaymentDate(p.getPaymentDate());
        dto.setNotes(p.getNotes());
        dto.setTotalRefundedAmount(p.getTotalRefundedAmount());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());

        if (p.getRefunds() != null) {
            dto.setRefunds(p.getRefunds().stream().map(this::toRefundDTO).collect(Collectors.toList()));
        }
        return dto;
    }

    public RefundDTO toRefundDTO(Refund r) {
        RefundDTO dto = new RefundDTO();
        dto.setId(r.getId());
        dto.setPaymentId(r.getPayment().getId());
        dto.setRefundAmount(r.getRefundAmount());
        dto.setRefundCurrency(r.getRefundCurrency());
        dto.setRefundAmountInBaseCurrency(r.getRefundAmountInBaseCurrency());
        dto.setCancellationFee(r.getCancellationFee());
        dto.setRefundReason(r.getRefundReason());
        dto.setRefundStatus(r.getRefundStatus());
        dto.setTransactionReference(r.getTransactionReference());
        dto.setRefundDate(r.getRefundDate());
        dto.setProcessedBy(r.getProcessedBy());
        dto.setNotes(r.getNotes());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());
        return dto;
    }
}