package com.example.agentapp.notification.service;

import com.example.agentapp.notification.event.AppNotificationEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class EmailTemplateService {

    /** Görünen marka adı — e-postaların başlık ve gövdesinde kullanılır. */
    @Value("${app.brand.name:Gezinio}")
    private String brandName;

    public String getSubject(AppNotificationEvent event) {
        return switch (event.getEventType()) {
            case USER_REGISTERED       -> "Welcome to " + brandName + ", " + event.getUsername() + "!";
            case STAFF_INVITED         -> "You have been invited to " + brandName + " — activate your account";
            case RESERVATION_CREATED   -> "Reservation Received - #" + event.getReservationId();
            case RESERVATION_CONFIRMED -> "Reservation Confirmed - #" + event.getReservationId();
            case RESERVATION_CANCELLED -> "Reservation Cancelled - #" + event.getReservationId();
            case PAYMENT_RECEIVED      -> "Payment Received - #" + event.getReservationId();
            case REFUND_PROCESSED      -> "Refund Processed - #" + event.getReservationId();
        };
    }

    public String getBody(AppNotificationEvent event) {
        return switch (event.getEventType()) {
            case USER_REGISTERED       -> buildUserRegistered(event);
            case STAFF_INVITED         -> buildStaffInvited(event);
            case RESERVATION_CREATED   -> buildReservationCreated(event);
            case RESERVATION_CONFIRMED -> buildReservationConfirmed(event);
            case RESERVATION_CANCELLED -> buildReservationCancelled(event);
            case PAYMENT_RECEIVED      -> buildPaymentReceived(event);
            case REFUND_PROCESSED      -> buildRefundProcessed(event);
        };
    }

    private String buildUserRegistered(AppNotificationEvent e) {
        return html("""
                <h2>Welcome, %s!</h2>
                <p>Thank you for joining %s. Your account has been successfully created.</p>
                <p>You can now explore tours, make reservations, and manage your travel plans with ease.</p>
                <p>We wish you wonderful adventures ahead!</p>
                """.formatted(e.getUsername(), brandName));
    }

    private String buildStaffInvited(AppNotificationEvent e) {
        String name = e.getRecipientName() != null && !e.getRecipientName().isBlank()
                ? e.getRecipientName() : e.getUsername();
        return html("""
                <h2>Welcome aboard, %s!</h2>
                <p>An administrator has created a %s staff account for you with the
                username <strong>%s</strong>.</p>
                <p>To finish setting up your account, choose your own password by clicking the
                button below:</p>
                <p style="text-align:center;margin:28px 0">
                  <a href="%s" style="background:#2c3e50;color:#fff;text-decoration:none;
                     padding:12px 28px;border-radius:4px;font-weight:bold;display:inline-block">
                     Activate my account
                  </a>
                </p>
                <p style="font-size:13px;color:#777">If the button does not work, copy and paste
                this link into your browser:<br><span style="color:#2c3e50">%s</span></p>
                <p style="color:#e67e22"><strong>Note:</strong> This invitation link expires in
                7 days. If it has expired, ask an administrator to re-send your invitation.</p>
                """.formatted(name, brandName, e.getUsername(), e.getInviteLink(), e.getInviteLink()));
    }

    private String buildReservationCreated(AppNotificationEvent e) {
        String groupInfo = e.getGroupCode() != null
                ? "<tr><td style='padding:6px'><strong>Group Code</strong></td><td>" + e.getGroupCode() + "</td></tr>"
                : "";
        return html("""
                <h2>Reservation Received</h2>
                <p>Dear <strong>%s</strong>, your reservation request has been successfully received.</p>
                <table style="border-collapse:collapse;width:100%%">
                  <tr><td style="padding:6px"><strong>Reservation No</strong></td><td>#%d</td></tr>
                  <tr><td style="padding:6px"><strong>Tour</strong></td><td>%s</td></tr>
                  <tr><td style="padding:6px"><strong>Destination</strong></td><td>%s</td></tr>
                  <tr><td style="padding:6px"><strong>Total Amount</strong></td><td>%s %s</td></tr>
                  %s
                </table>
                <p style="color:#e67e22"><strong>Status:</strong> Awaiting payment.
                Your reservation will be confirmed once payment is completed.</p>
                """.formatted(
                e.getUsername(), e.getReservationId(),
                e.getTourName(), e.getDestination(),
                formatAmount(e.getTotalPrice()), e.getCurrency(),
                groupInfo));
    }

    private String buildReservationConfirmed(AppNotificationEvent e) {
        return html("""
                <h2 style="color:#27ae60">Your Reservation is Confirmed ✓</h2>
                <p>Dear <strong>%s</strong>, great news! Your reservation has been confirmed.</p>
                <table style="border-collapse:collapse;width:100%%">
                  <tr><td style="padding:6px"><strong>Reservation No</strong></td><td>#%d</td></tr>
                  <tr><td style="padding:6px"><strong>Tour</strong></td><td>%s</td></tr>
                  <tr><td style="padding:6px"><strong>Destination</strong></td><td>%s</td></tr>
                  <tr><td style="padding:6px"><strong>Total Amount</strong></td><td>%s %s</td></tr>
                </table>
                <p>Our team will reach out to you before your travel date with further details.</p>
                <p>Have an amazing trip!</p>
                """.formatted(
                e.getUsername(), e.getReservationId(),
                e.getTourName(), e.getDestination(),
                formatAmount(e.getTotalPrice()), e.getCurrency()));
    }

    private String buildReservationCancelled(AppNotificationEvent e) {
        String notes = e.getNotes() != null
                ? "<p><strong>Reason:</strong> " + e.getNotes() + "</p>"
                : "";
        return html("""
                <h2 style="color:#e74c3c">Reservation Cancelled</h2>
                <p>Dear <strong>%s</strong>, your reservation has been cancelled.</p>
                <table style="border-collapse:collapse;width:100%%">
                  <tr><td style="padding:6px"><strong>Reservation No</strong></td><td>#%d</td></tr>
                  <tr><td style="padding:6px"><strong>Tour</strong></td><td>%s</td></tr>
                </table>
                %s
                <p>If you have made a payment, a refund process will be initiated.
                Please contact us if you have any questions.</p>
                """.formatted(e.getUsername(), e.getReservationId(), e.getTourName(), notes));
    }

    private String buildPaymentReceived(AppNotificationEvent e) {
        return html("""
                <h2 style="color:#27ae60">Payment Received ✓</h2>
                <p>Dear <strong>%s</strong>, your payment has been successfully processed.</p>
                <table style="border-collapse:collapse;width:100%%">
                  <tr><td style="padding:6px"><strong>Reservation No</strong></td><td>#%d</td></tr>
                  <tr><td style="padding:6px"><strong>Tour</strong></td><td>%s</td></tr>
                  <tr><td style="padding:6px"><strong>Amount Paid</strong></td><td>%s %s</td></tr>
                </table>
                <p>Please keep this email as your payment receipt.</p>
                """.formatted(
                e.getUsername(), e.getReservationId(),
                e.getTourName(),
                formatAmount(e.getPaidAmount()), e.getCurrency()));
    }

    private String buildRefundProcessed(AppNotificationEvent e) {
        return html("""
                <h2>Refund Processed</h2>
                <p>Dear <strong>%s</strong>, your refund has been successfully processed.</p>
                <table style="border-collapse:collapse;width:100%%">
                  <tr><td style="padding:6px"><strong>Reservation No</strong></td><td>#%d</td></tr>
                  <tr><td style="padding:6px"><strong>Refund Amount</strong></td><td>%s %s</td></tr>
                </table>
                <p>The refund will be reflected in your account within 3-7 business days,
                depending on your payment method.</p>
                """.formatted(
                e.getUsername(), e.getReservationId(),
                formatAmount(e.getRefundAmount()), e.getCurrency()));
    }

    private String html(String body) {
        return """
                <!DOCTYPE html>
                <html><body style="font-family:Arial,sans-serif;color:#333;max-width:600px;margin:auto;padding:20px">
                <div style="border-bottom:3px solid #2c3e50;padding-bottom:12px;margin-bottom:20px">
                  <span style="font-size:22px;font-weight:bold;color:#2c3e50">%s</span>
                </div>
                %s
                <div style="border-top:1px solid #eee;margin-top:30px;padding-top:12px;font-size:12px;color:#999">
                  This email was sent automatically. Please do not reply.
                </div>
                </body></html>
                """.formatted(brandName, body);
    }

    private String formatAmount(BigDecimal amount) {
        return amount != null ? String.format("%,.2f", amount) : "0.00";
    }
}