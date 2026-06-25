package com.example.agentapp.notification.event;

import com.example.agentapp.notification.model.NotificationEventType;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

/**
 * Uygulama genelinde bildirim tetiklemek için kullanılan event.
 * Business service'ler bu event'i publish eder,
 * NotificationEventListener async olarak işler.
 */
public class AppNotificationEvent extends ApplicationEvent {

    private final NotificationEventType eventType;

    // Kullanıcı bilgisi
    private final Long userId;
    private final String username;
    private final String email;
    private final String phone; // SMS için (null ise SMS gönderilmez)

    // İlgili entity bilgileri (template doldurmak için)
    private Long reservationId;
    private String tourName;
    private String destination;
    private BigDecimal totalPrice;
    private String currency;
    private BigDecimal paidAmount;
    private BigDecimal refundAmount;
    private String groupCode;
    private String notes;

    // Staff invite bilgileri
    private String inviteLink;   // aktivasyon linki (token gömülü)
    private String recipientName; // mailde hitap için (ad soyad)

    public AppNotificationEvent(Object source,
                                 NotificationEventType eventType,
                                 Long userId,
                                 String username,
                                 String email,
                                 String phone) {
        super(source);
        this.eventType = eventType;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.phone = phone;
    }

    // Fluent builder metodları
    public AppNotificationEvent withReservation(Long reservationId, String tourName, String destination,
                                                 BigDecimal totalPrice, String currency) {
        this.reservationId = reservationId;
        this.tourName = tourName;
        this.destination = destination;
        this.totalPrice = totalPrice;
        this.currency = currency;
        return this;
    }

    public AppNotificationEvent withPayment(BigDecimal paidAmount, String currency) {
        this.paidAmount = paidAmount;
        this.currency = currency;
        return this;
    }

    public AppNotificationEvent withRefund(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
        return this;
    }

    public AppNotificationEvent withGroupCode(String groupCode) {
        this.groupCode = groupCode;
        return this;
    }

    public AppNotificationEvent withNotes(String notes) {
        this.notes = notes;
        return this;
    }

    public AppNotificationEvent withInvite(String inviteLink, String recipientName) {
        this.inviteLink = inviteLink;
        this.recipientName = recipientName;
        return this;
    }

    // Getters

    public NotificationEventType getEventType() { return eventType; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public Long getReservationId() { return reservationId; }
    public String getTourName() { return tourName; }
    public String getDestination() { return destination; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public String getCurrency() { return currency; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public String getGroupCode() { return groupCode; }
    public String getNotes() { return notes; }
    public String getInviteLink() { return inviteLink; }
    public String getRecipientName() { return recipientName; }
}