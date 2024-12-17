package com.almagest_dev.tacobank_core_server.presentation.controller;

import com.almagest_dev.tacobank_core_server.application.service.NotificationService;
import com.almagest_dev.tacobank_core_server.common.dto.CoreResponseDto;
import com.almagest_dev.tacobank_core_server.presentation.dto.notify.NotificationResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/taco/core/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 알림 조회
     */
    @GetMapping("/{memberId}")
    public ResponseEntity<?> getNotifications(@PathVariable Long memberId) {
        List<NotificationResponseDto> notifications = notificationService.getNotificationsForMember(memberId);
        return ResponseEntity.ok(new CoreResponseDto<>("SUCCESS", "알림 조회 성공", notifications));
    }

    /**
     * 알림 읽음
     */
    @PatchMapping("/{notificationId}")
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(new CoreResponseDto<>("SUCCESS", "알림 읽음 처리 성공"));
    }
}
