package com.example.backend.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.domain.entity.Notification;
import com.example.backend.domain.entity.User;
import com.example.backend.util.constant.notification.NotificationTypeEnum;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    long countByUserAndIsReadFalse(User user);

    List<Notification> findByTypeAndCreatedAtBetween(
            NotificationTypeEnum type,
            Instant from,
            Instant to);
}
