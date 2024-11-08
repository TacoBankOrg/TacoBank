package com.taco_bank.business_server.application.service;

import com.taco_bank.business_server.domain.friend.model.Friend;
import com.taco_bank.business_server.domain.friend.repository.FriendRepository;
import com.taco_bank.business_server.presentation.dto.FriendRequestDto;
import com.taco_bank.business_server.presentation.dto.FriendResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FriendService {

    private final FriendRepository friendRepository;

    public FriendService(FriendRepository friendRepository) {
        this.friendRepository = friendRepository;
    }

    @Transactional
    public void requestFriend(FriendRequestDto requestDto) {
        Optional<Friend> existingFriend = friendRepository.findByRequesterIdAndReceiverId(
                requestDto.getRequesterId(), requestDto.getReceiverId()
        );

        if (existingFriend.isPresent()) {
            Friend friend = existingFriend.get();

            // 차단된 사용자에게는 친구 요청을 보낼 수 없음
            if ("BAN".equals(friend.getStatus())) {
                throw new IllegalStateException("차단해체 후 친구 요청을 보낼 수 있습니다.");
            }

            if("BLOCKED_BY".equals(friend.getStatus())) {
                throw new IllegalStateException("현재 차단 상태입니다. 친구 요청을 보낼 수 없습니다.");
            }

            // 이미 친구 요청을 보냈거나 친구 관계가 존재하는 경우 중복 요청 방지
            if (!"REJ".equals(friend.getStatus()) && !"DEL".equals(friend.getStatus()) && !"NONE".equals(friend.getStatus())) {
                throw new IllegalStateException("이미 친구 요청을 보냈거나 친구 관계가 존재합니다.");
            }

            // 상태가 "거절" 또는 "삭제"라면, 새로운 요청으로 업데이트
            friend.setStatus("REQ");
            friend.setUpdatedDate(LocalDateTime.now());
            friendRepository.save(friend);
            return;
        }

        // 기존 관계가 없을 경우 새로운 친구 요청 생성
        Friend newFriend = new Friend();
        newFriend.setRequesterId(requestDto.getRequesterId());
        newFriend.setReceiverId(requestDto.getReceiverId());
        newFriend.setStatus("REQ"); // 요청 상태 설정
        newFriend.setLiked("N"); // 기본적으로 좋아요는 "N"
        newFriend.setCreatedDate(LocalDateTime.now());
        newFriend.setUpdatedDate(LocalDateTime.now());
        friendRepository.save(newFriend);
    }

    @Transactional
    public void acceptFriend(FriendRequestDto requestDto) {
        Friend friend = friendRepository.findByRequesterIdAndReceiverId(requestDto.getRequesterId(), requestDto.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("친구 요청 정보가 존재하지 않습니다."));
        friend.setStatus("ACC"); // 수락 상태로 설정
        friendRepository.save(friend);
    }

    @Transactional
    public void rejectFriend(FriendRequestDto requestDto) {
        Friend friend = friendRepository.findByRequesterIdAndReceiverId(requestDto.getRequesterId(), requestDto.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("친구 요청 정보가 존재하지 않습니다."));
        friend.setStatus("REJ"); // 거절 상태로 설정
        friendRepository.save(friend);
    }

    @Transactional
    public void blockFriend(FriendRequestDto requestDto) {
        // 요청자가 친구를 차단하는 관계 설정
        Friend friend = friendRepository.findByRequesterIdAndReceiverId(
                requestDto.getRequesterId(), requestDto.getReceiverId()
        ).orElseGet(() -> new Friend(requestDto.getRequesterId(), requestDto.getReceiverId()));

        friend.setStatus("BAN"); // 차단 상태로 설정
        friend.setLiked("N"); // 좋아요 상태를 취소
        friendRepository.save(friend);

        // 차단당한 친구가 요청자에게 친구 요청을 보내지 못하도록 상태 설정
        Friend reverseFriend = friendRepository.findByRequesterIdAndReceiverId(
                requestDto.getReceiverId(), requestDto.getRequesterId()
        ).orElseGet(() -> new Friend(requestDto.getReceiverId(), requestDto.getRequesterId()));

        reverseFriend.setStatus("BLOCKED_BY"); // 차단당한 관계 표시
        reverseFriend.setLiked("N");
        friendRepository.save(reverseFriend);
    }

    @Transactional
    public void unblockFriend(FriendRequestDto requestDto) {
        // 요청자가 차단을 해제하는 관계 처리
        friendRepository.findByRequesterIdAndReceiverId(
                requestDto.getRequesterId(), requestDto.getReceiverId()
        ).ifPresent(friend -> {
            if ("BAN".equals(friend.getStatus())) { // 차단을 설정한 관계만 해제 가능
                friend.setStatus("NONE"); // 기본 상태로 변경
                friendRepository.save(friend);
            } else {
                throw new IllegalStateException("차단을 설정한 사용자만 해제할 수 있습니다.");
            }
        });

        // 상대방이 요청자를 차단당한 관계로 표시한 경우 해제
        friendRepository.findByRequesterIdAndReceiverId(
                requestDto.getReceiverId(), requestDto.getRequesterId()
        ).ifPresent(reverseFriend -> {
            if ("BLOCKED_BY".equals(reverseFriend.getStatus())) {
                reverseFriend.setStatus("NONE"); // 기본 상태로 변경
                friendRepository.save(reverseFriend);
            }
        });
    }


    @Transactional
    public void deleteFriend(FriendRequestDto requestDto) {
        Friend friend = friendRepository.findByRequesterIdAndReceiverId(
                requestDto.getRequesterId(), requestDto.getReceiverId()
        ).orElseThrow(() -> new IllegalArgumentException("친구 정보가 존재하지 않습니다."));

        friend.setStatus("DEL"); // 삭제 상태로 설정
        friend.setLiked("N"); // 좋아요 상태를 취소
        friendRepository.save(friend);
    }

    @Transactional
    public void likeFriend(FriendRequestDto requestDto) {
        Friend friend = friendRepository.findByRequesterIdAndReceiverId(
                requestDto.getRequesterId(), requestDto.getReceiverId()
        ).orElseThrow(() -> new IllegalArgumentException("친구 정보가 존재하지 않습니다."));

        // 상태가 DEL이나 BAN인 경우 좋아요를 막음
        if ("DEL".equals(friend.getStatus()) || "BAN".equals(friend.getStatus())) {
            throw new IllegalStateException("삭제되거나 차단된 친구에게는 좋아요를 누를 수 없습니다.");
        }

        friend.setLiked("Y"); // 좋아요 상태로 설정
        friendRepository.save(friend);
    }

    @Transactional
    public void unlikeFriend(FriendRequestDto requestDto) {
        Friend friend = friendRepository.findByRequesterIdAndReceiverId(
                requestDto.getRequesterId(), requestDto.getReceiverId()
        ).orElseThrow(() -> new IllegalArgumentException("친구 정보가 존재하지 않습니다."));

        // 상태가 DEL이나 BAN인 경우 좋아요 취소를 막음
        if ("DEL".equals(friend.getStatus()) || "BAN".equals(friend.getStatus())) {
            throw new IllegalStateException("삭제되거나 차단된 친구에게는 좋아요 취소를 할 수 없습니다.");
        }

        friend.setLiked("N"); // 좋아요 상태를 취소
        friendRepository.save(friend);
    }

    public List<FriendResponseDto> getFriendList(String userId) {
        // 친구 목록 조회 - 상태가 ACC인 친구만 가져옴
        List<Friend> friends = friendRepository.findByRequesterIdAndStatusOrReceiverIdAndStatus(userId, "ACC", userId, "ACC");

        // Friend 엔티티를 FriendResponseDto로 변환하여 반환
        return friends.stream()
                .map(friend -> new FriendResponseDto(friend))
                .collect(Collectors.toList());
    }

}