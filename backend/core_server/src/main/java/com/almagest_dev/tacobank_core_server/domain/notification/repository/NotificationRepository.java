package com.almagest_dev.tacobank_core_server.domain.notification.repository;

import com.almagest_dev.tacobank_core_server.domain.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByMember_IdOrderByCreatedDateDesc(Long memberId);
}
