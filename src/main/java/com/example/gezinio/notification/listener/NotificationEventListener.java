package com.example.gezinio.notification.listener;

import com.example.gezinio.notification.event.AppNotificationEvent;
import com.example.gezinio.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Business service'lerden gelen AppNotificationEvent'leri dinler.
 * @Async ile ana iş akışını bloklamadan arka planda çalışır.
 */
@Component
public class NotificationEventListener {

    private final NotificationService notificationService;

    @Autowired
    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Async
    @EventListener
    public void handleNotificationEvent(AppNotificationEvent event) {
        notificationService.sendEmail(event);
    }
}