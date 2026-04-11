package com.example.agentapp.notification.repository;

import com.example.agentapp.notification.model.NotificationEventType;
import com.example.agentapp.notification.model.NotificationLog;
import com.example.agentapp.notification.model.NotificationStatus;
import com.example.agentapp.notification.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    List<NotificationLog> findByUserId(Long userId);

    List<NotificationLog> findByStatus(NotificationStatus status);

    List<NotificationLog> findByUserIdAndType(Long userId, NotificationType type);

    List<NotificationLog> findByEventType(NotificationEventType eventType);
}