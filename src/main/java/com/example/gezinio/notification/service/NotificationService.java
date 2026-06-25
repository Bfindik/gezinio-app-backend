package com.example.gezinio.notification.service;

import com.example.gezinio.notification.event.AppNotificationEvent;
import com.example.gezinio.notification.model.NotificationLog;
import com.example.gezinio.notification.model.NotificationStatus;
import com.example.gezinio.notification.model.NotificationType;
import com.example.gezinio.notification.repository.NotificationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final EmailService emailService;
    private final EmailTemplateService templateService;
    private final NotificationLogRepository logRepository;

    @Autowired
    public NotificationService(EmailService emailService,
                                EmailTemplateService templateService,
                                NotificationLogRepository logRepository) {
        this.emailService = emailService;
        this.templateService = templateService;
        this.logRepository = logRepository;
    }

    public void sendEmail(AppNotificationEvent event) {
        if (event.getEmail() == null || event.getEmail().isBlank()) {
            logger.warn("No email address for user {}. Skipping.", event.getUserId());
            return;
        }

        String subject = templateService.getSubject(event);
        String body = templateService.getBody(event);

        NotificationLog log = buildLog(event, NotificationType.EMAIL, event.getEmail(), subject, body);

        try {
            emailService.send(event.getEmail(), subject, body);
            log.setStatus(NotificationStatus.SENT);
            log.setSentAt(LocalDateTime.now());
        } catch (Exception ex) {
            log.setStatus(NotificationStatus.FAILED);
            log.setErrorMessage(ex.getMessage());
            logger.error("Email failed for user {}: {}", event.getUserId(), ex.getMessage());
        } finally {
            logRepository.save(log);
        }
    }

    private NotificationLog buildLog(AppNotificationEvent event, NotificationType type,
                                     String recipient, String subject, String content) {
        NotificationLog log = new NotificationLog();
        log.setUserId(event.getUserId());
        log.setType(type);
        log.setEventType(event.getEventType());
        log.setRecipient(recipient);
        log.setSubject(subject);
        log.setContent(content);
        log.setStatus(NotificationStatus.PENDING);
        return log;
    }
}