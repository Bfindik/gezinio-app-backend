package com.example.gezinio.notification.service;

import com.example.gezinio.notification.event.AppNotificationEvent;
import com.example.gezinio.notification.model.NotificationEventType;
import com.example.gezinio.notification.model.NotificationLog;
import com.example.gezinio.notification.model.NotificationStatus;
import com.example.gezinio.notification.repository.NotificationLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock EmailService emailService;
    @Mock EmailTemplateService templateService;
    @Mock NotificationLogRepository logRepository;

    @InjectMocks NotificationService service;

    private AppNotificationEvent eventWithEmail;
    private AppNotificationEvent eventWithoutEmail;

    @BeforeEach
    void setUp() {
        eventWithEmail = new AppNotificationEvent(
                this,
                NotificationEventType.USER_REGISTERED,
                1L, "testuser", "test@mail.com", null
        );

        eventWithoutEmail = new AppNotificationEvent(
                this,
                NotificationEventType.USER_REGISTERED,
                2L, "nomail", null, null
        );
    }

    // ─── sendEmail ────────────────────────────────────────────────────────────

    @Test
    void sendEmail_whenEmailIsNull_skipsAndDoesNotLog() {
        service.sendEmail(eventWithoutEmail);

        verify(emailService, never()).send(any(), any(), any());
        verify(logRepository, never()).save(any());
    }

    @Test
    void sendEmail_whenEmailIsBlank_skipsAndDoesNotLog() {
        AppNotificationEvent blankEmail = new AppNotificationEvent(
                this, NotificationEventType.USER_REGISTERED, 3L, "user", "   ", null
        );

        service.sendEmail(blankEmail);

        verify(emailService, never()).send(any(), any(), any());
        verify(logRepository, never()).save(any());
    }

    @Test
    void sendEmail_success_logsAsSent() {
        when(templateService.getSubject(any())).thenReturn("Welcome!");
        when(templateService.getBody(any())).thenReturn("<p>Hello</p>");

        service.sendEmail(eventWithEmail);

        ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(logRepository).save(captor.capture());

        NotificationLog saved = captor.getValue();
        assertEquals(NotificationStatus.SENT, saved.getStatus());
        assertEquals("test@mail.com", saved.getRecipient());
        assertNotNull(saved.getSentAt());
    }

    @Test
    void sendEmail_whenEmailServiceThrows_logsAsFailed() {
        when(templateService.getSubject(any())).thenReturn("Subject");
        when(templateService.getBody(any())).thenReturn("Body");
        doThrow(new RuntimeException("SMTP error")).when(emailService).send(anyString(), anyString(), anyString());

        service.sendEmail(eventWithEmail);

        ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(logRepository).save(captor.capture());

        NotificationLog saved = captor.getValue();
        assertEquals(NotificationStatus.FAILED, saved.getStatus());
        assertEquals("SMTP error", saved.getErrorMessage());
    }

    @Test
    void sendEmail_alwaysSavesLog_evenOnFailure() {
        when(templateService.getSubject(any())).thenReturn("Subject");
        when(templateService.getBody(any())).thenReturn("Body");
        doThrow(new RuntimeException("connection timeout")).when(emailService)
                .send(anyString(), anyString(), anyString());

        service.sendEmail(eventWithEmail);

        // logRepository.save must be called exactly once regardless of exception
        verify(logRepository, times(1)).save(any(NotificationLog.class));
    }
}
