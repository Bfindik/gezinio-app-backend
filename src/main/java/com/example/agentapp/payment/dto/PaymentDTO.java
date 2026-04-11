package com.example.agentapp.payment.dto;

import com.example.agentapp.payment.model.PaymentMethod;
import com.example.agentapp.payment.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PaymentDTO {

    private Long id;
    private Long reservationId;

    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;

    private BigDecimal amount;
    private String currency;
    private BigDecimal exchangeRate;
    private BigDecimal amountInBaseCurrency;

    private String transactionReference;

    private String cardLastFour;
    private String cardHolderName;
    private String bankName;
    private String senderAccountMasked;

    private LocalDateTime paymentDate;
    private String notes;

    private BigDecimal totalRefundedAmount;
    private List<RefundDTO> refunds;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PaymentDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }

    public BigDecimal getAmountInBaseCurrency() { return amountInBaseCurrency; }
    public void setAmountInBaseCurrency(BigDecimal amountInBaseCurrency) { this.amountInBaseCurrency = amountInBaseCurrency; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

    public String getCardLastFour() { return cardLastFour; }
    public void setCardLastFour(String cardLastFour) { this.cardLastFour = cardLastFour; }

    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getSenderAccountMasked() { return senderAccountMasked; }
    public void setSenderAccountMasked(String senderAccountMasked) { this.senderAccountMasked = senderAccountMasked; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public BigDecimal getTotalRefundedAmount() { return totalRefundedAmount; }
    public void setTotalRefundedAmount(BigDecimal totalRefundedAmount) { this.totalRefundedAmount = totalRefundedAmount; }

    public List<RefundDTO> getRefunds() { return refunds; }
    public void setRefunds(List<RefundDTO> refunds) { this.refunds = refunds; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}