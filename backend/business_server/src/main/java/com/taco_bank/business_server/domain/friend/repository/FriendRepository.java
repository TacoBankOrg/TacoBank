package com.taco_bank.business_server.domain.friend.repository;

import com.taco_bank.business_server.domain.friend.model.Friend;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    Optional<Friend> findByRequesterIdAndReceiverId(String requesterId, String receiverId);

    // 요청한 사람의 친구 또는 요청받은 사람의 친구 중 상태가 'ACC'인 목록 조회
    List<Friend> findByRequesterIdAndStatusOrReceiverIdAndStatus(String requesterId, String status1, String receiverId, String status2);
}
