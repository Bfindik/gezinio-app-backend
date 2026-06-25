package com.example.gezinio.payment.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    // Geri ödenecek tutar (ödemenin dövizi cinsinden)
    @NotNull
    @DecimalMin(value = "0.01")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal refundAmount;

    @NotNull
    @Column(nullable = false, length = 3)
    private String refundCurrency;

    // Rezervasyonun base currency'sine çevrilmiş tutar
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal refundAmountInBaseCurrency;

    // İptal ücreti (varsa, base currency cinsinden)
    @Column(precision = 12, scale = 2)
    private BigDecimal cancellationFee = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RefundReason refundReason;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RefundStatus refundStatus = RefundStatus.PENDING;

    // Banka/ödeme sistemi geri ödeme referansı
    @Column(length = 100)
    private String transactionReference;

    @Column
    private LocalDateTime refundDate;

    // Geri ödemeyi işleyen kullanıcı (ajan/admin ID)
    @Column
    private Long processedBy;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Refund() {}

    // Getters and Setters

    public Long getId() { return id; }

    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }

    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }

    public String getRefundCurrency() { return refundCurrency; }
    public void setRefundCurrency(String refundCurrency) { this.refundCurrency = refundCurrency; }

    public BigDecimal getRefundAmountInBaseCurrency() { return refundAmountInBaseCurrency; }
    public void setRefundAmountInBaseCurrency(BigDecimal refundAmountInBaseCurrency) { this.refundAmountInBaseCurrency = refundAmountInBaseCurrency; }

    public BigDecimal getCancellationFee() { return cancellationFee; }
    public void setCancellationFee(BigDecimal cancellationFee) { this.cancellationFee = cancellationFee; }

    public RefundReason getRefundReason() { return refundReason; }
    public void setRefundReason(RefundReason refundReason) { this.refundReason = refundReason; }

    public RefundStatus getRefundStatus() { return refundStatus; }
    public void setRefundStatus(RefundStatus refundStatus) { this.refundStatus = refundStatus; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

    public LocalDateTime getRefundDate() { return refundDate; }
    public void setRefundDate(LocalDateTime refundDate) { this.refundDate = refundDate; }

    public Long getProcessedBy() { return processedBy; }
    public void setProcessedBy(Long processedBy) { this.processedBy = processedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}