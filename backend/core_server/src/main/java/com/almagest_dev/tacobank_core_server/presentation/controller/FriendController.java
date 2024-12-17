package com.almagest_dev.tacobank_core_server.presentation.controller;

import com.almagest_dev.tacobank_core_server.common.dto.CoreResponseDto;
import com.almagest_dev.tacobank_core_server.presentation.dto.friend.FriendRequestDto;
import com.almagest_dev.tacobank_core_server.presentation.dto.friend.FriendResponseDto;
import com.almagest_dev.tacobank_core_server.application.service.FriendService;
import com.almagest_dev.tacobank_core_server.presentation.dto.friend.FriendResponseDto2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/taco/core/friends")
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    // 친구 요청
    @PostMapping("/request")
    public ResponseEntity<CoreResponseDto<String>> requestFriend(@RequestBody FriendRequestDto requestDto) {
        friendService.requestFriend(requestDto.getRequesterId(), requestDto);
        return ResponseEntity.ok(
                new CoreResponseDto<>("SUCCESS", "친구 요청이 성공적으로 처리되었습니다.", null)
        );
    }

    // 친구 수락
    @PostMapping("/accept")
    public ResponseEntity<CoreResponseDto<String>> acceptFriend(@RequestBody FriendRequestDto requestDto) {
        friendService.acceptFriend(requestDto.getRequesterId(), requestDto);
        return ResponseEntity.ok(
                new CoreResponseDto<>("SUCCESS", "친구 요청을 수락하였습니다.", null)
        );
    }

    // 친구 거절
    @PostMapping("/reject")
    public ResponseEntity<CoreResponseDto<String>> rejectFriend(@RequestBody FriendRequestDto requestDto) {
        friendService.rejectFriend(requestDto.getRequesterId(), requestDto);
        return ResponseEntity.ok(
                new CoreResponseDto<>("SUCCESS", "친구 요청을 거절하였습니다.", null)
        );
    }

    // 친구 차단
    @PostMapping("/block")
    public ResponseEntity<CoreResponseDto<String>> blockFriend(@RequestBody FriendRequestDto requestDto) {
        friendService.blockFriend(requestDto.getRequesterId(), requestDto);
        return ResponseEntity.ok(
                new CoreResponseDto<>("SUCCESS", "친구를 차단하였습니다.", null)
        );
    }

    // 친구 차단 해제
    @PostMapping("/unblock")
    public ResponseEntity<CoreResponseDto<String>> unblockFriend(@RequestBody FriendRequestDto requestDto) {
        friendService.unblockFriend(requestDto.getRequesterId(), requestDto);
        return ResponseEntity.ok(
                new CoreResponseDto<>("SUCCESS", "친구 차단을 해제하였습니다.", null)
        );
    }

    // 친구 삭제
    @PostMapping
    public ResponseEntity<CoreResponseDto<String>> deleteFriend(@RequestBody FriendRequestDto requestDto) {
        friendService.deleteFriend(requestDto.getRequesterId(), requestDto);
        return ResponseEntity.ok(
                new CoreResponseDto<>("SUCCESS", "친구를 삭제하였습니다.", null)
        );
    }

    // 차단한 친구 조회
    @GetMapping("/blocked/{requesterId}")
    public ResponseEntity<CoreResponseDto<List<FriendResponseDto2>>> getBlockedFriends(@PathVariable Long requesterId) {
        List<FriendResponseDto2> blockedFriends = friendService.getBlockedFriends(requesterId);
        return ResponseEntity.ok(
                new CoreResponseDto<>("SUCCESS", "차단한 친구 목록 조회 성공", blockedFriends)
        );
    }

    // 친구 목록 조회
    @GetMapping("/list/{requesterId}")
    public ResponseEntity<CoreResponseDto<List<FriendResponseDto>>> getFriendList(@PathVariable Long requesterId) {
        List<FriendResponseDto> friends = friendService.getFriendList(requesterId);
        return ResponseEntity.ok(
                new CoreResponseDto<>("SUCCESS", "친구 목록 조회 성공", friends)
        );
    }

    // 친구 좋아요
    @PostMapping("/like")
    public ResponseEntity<CoreResponseDto<String>> likeFriend(@RequestBody FriendRequestDto requestDto) {
        friendService.likeFriend(requestDto.getRequesterId(), requestDto);
        return ResponseEntity.ok(
                new CoreResponseDto<>("SUCCESS", "친구에게 좋아요를 눌렀습니다.", null)
        );
    }

    // 친구 좋아요 취소
    @PostMapping("/unlike")
    public ResponseEntity<CoreResponseDto<String>> unlikeFriend(@RequestBody FriendRequestDto requestDto) {
        friendService.unlikeFriend(requestDto.getRequesterId(), requestDto);
        return ResponseEntity.ok(
                new CoreResponseDto<>("SUCCESS", "친구에게 좋아요 취소를 했습니다.", null)
        );
    }

    // 받은 친구 요청 조회
    @GetMapping("/received/list/{requesterId}")
    public ResponseEntity<CoreResponseDto<List<FriendResponseDto2>>> getReceivedFriendRequests(@PathVariable Long requesterId) {
        List<FriendResponseDto2> receivedRequests = friendService.getReceivedFriendRequests(requesterId);
        return ResponseEntity.ok(
                new CoreResponseDto<>("SUCCESS", "받은 친구 요청 목록 조회 성공", receivedRequests)
        );
    }

    // 삭제한 친구 목록 조회
    @GetMapping("/deleted/list/{requesterId}")
    public ResponseEntity<CoreResponseDto<List<FriendResponseDto2>>> getDeletedFriends(@PathVariable Long requesterId) {
        List<FriendResponseDto2> deletedFriends = friendService.getDeletedFriends(requesterId);
        return ResponseEntity.ok(
                new CoreResponseDto<>(
                        "SUCCESS",
                        "삭제한 친구 목록 조회 성공",
                        deletedFriends
                )
        );
    }

    // 삭제 취소 기능
    @PostMapping("/undo-delete")
    public ResponseEntity<CoreResponseDto<Void>> undoDeleteFriend(@RequestBody FriendRequestDto requestDto) {
        friendService.undoDeleteFriend(requestDto.getRequesterId(), requestDto);
        return ResponseEntity.ok(
                new CoreResponseDto<>(
                        "SUCCESS",
                        "삭제 취소가 성공적으로 완료되었습니다.",
                        null
                )
        );
    }

}