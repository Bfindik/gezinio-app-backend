package com.example.gezinio.payment.model;

import com.example.gezinio.reservation.model.Reservation;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    // Müşterinin ödediği tutar ve döviz (örn: 28.57 USD)
    @NotNull
    @DecimalMin(value = "0.01")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Column(nullable = false, length = 3)
    private String currency;

    // 1 birim currency = exchangeRate birim rezervasyon dövizi
    // Aynı dövizse 1.0 girilir
    @Column(nullable = false, precision = 12, scale = 6)
    private BigDecimal exchangeRate = BigDecimal.ONE;

    // amount * exchangeRate → rezervasyonun dövizine çevrilmiş tutar
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amountInBaseCurrency;

    // Tekil işlem referansı (banka dekontu no, pos slip no vs.)
    @Column(unique = true, length = 100)
    private String transactionReference;

    // Kart ödemeleri için (sadece son 4 hane saklanır)
    @Column(length = 4)
    private String cardLastFour;

    @Column(length = 100)
    private String cardHolderName;

    // Banka havalesi için
    @Column(length = 100)
    private String bankName;

    @Column(length = 50)
    private String senderAccountMasked; // örn: "****1234"

    @Column
    private LocalDateTime paymentDate;

    @Column(length = 500)
    private String notes;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Refund> refunds = new ArrayList<>();

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

    public Payment() {}

    // Tamamlanan geri ödemelerin toplam tutarı (base currency)
    public BigDecimal getTotalRefundedAmount() {
        return refunds.stream()
                .filter(r -> r.getRefundStatus() == RefundStatus.COMPLETED)
                .map(Refund::getRefundAmountInBaseCurrency)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Getters and Setters

    public Long getId() { return id; }

    public Reservation getReservation() { return reservation; }
    public void setReservation(Reservation reservation) { this.reservation = reservation; }

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

    public List<Refund> getRefunds() { return refunds; }
    public void setRefunds(List<Refund> refunds) { this.refunds = refunds; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}