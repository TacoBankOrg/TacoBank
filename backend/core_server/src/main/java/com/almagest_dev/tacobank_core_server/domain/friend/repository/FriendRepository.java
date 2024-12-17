package com.almagest_dev.tacobank_core_server.domain.friend.repository;


import com.almagest_dev.tacobank_core_server.domain.friend.model.Friend;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    Optional<Friend> findByRequesterIdAndReceiverId(Long requesterId, Long receiverId);

    // 요청한 사람의 친구 또는 요청받은 사람의 친구 중 상태가 'ACC'인 목록 조회
    List<Friend> findByRequesterIdAndStatusOrReceiverIdAndStatus(Long requesterId, String status1, Long receiverId, String status2);

    List<Friend> findByRequesterIdAndStatus(Long requesterId, String status);


}
