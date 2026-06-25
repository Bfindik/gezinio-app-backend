package com.example.gezinio.payment.dto;

import com.example.gezinio.payment.model.RefundReason;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class RefundCreateRequest {

    @NotNull(message = "Payment ID is required")
    private Long paymentId;

    @NotNull(message = "Refund amount is required")
    @DecimalMin(value = "0.01", message = "Refund amount must be greater than 0")
    private BigDecimal refundAmount;

    @NotNull(message = "Refund currency is required")
    @Size(min = 3, max = 3)
    private String refundCurrency;

    // İptal ücreti varsa (kesildikten sonra kalan iade edilir)
    private BigDecimal cancellationFee = BigDecimal.ZERO;

    @NotNull(message = "Refund reason is required")
    private RefundReason refundReason;

    private String transactionReference;
    private String notes;

    // Getters and Setters

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }

    public String getRefundCurrency() { return refundCurrency; }
    public void setRefundCurrency(String refundCurrency) { this.refundCurrency = refundCurrency; }

    public BigDecimal getCancellationFee() { return cancellationFee; }
    public void setCancellationFee(BigDecimal cancellationFee) { this.cancellationFee = cancellationFee; }

    public RefundReason getRefundReason() { return refundReason; }
    public void setRefundReason(RefundReason refundReason) { this.refundReason = refundReason; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}