package com.almagest_dev.tacobank_core_server.application.service;

import com.almagest_dev.tacobank_core_server.domain.member.model.Member;
import com.almagest_dev.tacobank_core_server.domain.notification.model.Notification;
import com.almagest_dev.tacobank_core_server.domain.notification.repository.NotificationRepository;
import com.almagest_dev.tacobank_core_server.presentation.dto.notify.NotificationResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * 알림 생성하고 저장하기
     */
    @Transactional
    public void sendNotification(Member member, String message) {
        Notification notification = new Notification();
        notification.saveMember(member);
        notification.saveMessage(message);
        notificationRepository.save(notification);
    }

    /**
     * 알림 조회
     */
    public List<NotificationResponseDto> getNotificationsForMember(Long memberId) {
        List<Notification> notifications = notificationRepository.findAllByMember_IdOrderByCreatedDateDesc(memberId);
        return notifications.stream()
                .map(notification -> new NotificationResponseDto(
                        notification.getMember().getId()
                        , notification.getId()
                        , notification.getMessage()
                        , notification.getCreatedDate()))
                .collect(Collectors.toList());

    }

    /**
     * 알림 읽음 처리
     */
    public void markAsRead(Long notificationId) {
        // 알림 내역에 있는지 확인
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(() -> new IllegalArgumentException("조회된 알림이 없습니다."));

        // 알림 여부 업데이트
        if (!"Y".equals(notification.getIsRead())) {
            notification.updateIsRead("Y");
            notificationRepository.save(notification);
        }
    }
}
