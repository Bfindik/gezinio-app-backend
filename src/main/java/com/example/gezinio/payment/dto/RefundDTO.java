package com.example.gezinio.payment.dto;

import com.example.gezinio.payment.model.RefundReason;
import com.example.gezinio.payment.model.RefundStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RefundDTO {

    private Long id;
    private Long paymentId;

    private BigDecimal refundAmount;
    private String refundCurrency;
    private BigDecimal refundAmountInBaseCurrency;
    private BigDecimal cancellationFee;

    private RefundReason refundReason;
    private RefundStatus refundStatus;

    private String transactionReference;
    private LocalDateTime refundDate;
    private Long processedBy;
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public RefundDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

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
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}