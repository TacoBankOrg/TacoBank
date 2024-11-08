package com.taco_bank.business_server.presentation.controller;

import com.taco_bank.business_server.application.service.FriendService;
import com.taco_bank.business_server.presentation.dto.FriendRequestDto;
import com.taco_bank.business_server.presentation.dto.FriendResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/business/friends")
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    // 친구 요청
    @PostMapping("/request")
    public ResponseEntity<String> requestFriend(@RequestBody FriendRequestDto requestDto) {
        friendService.requestFriend(requestDto);
        return ResponseEntity.ok("친구 요청이 성공적으로 처리되었습니다.");
    }

    // 친구 요청 수락
    @PostMapping("/accept")
    public ResponseEntity<String> acceptFriend(@RequestBody FriendRequestDto requestDto) {
        friendService.acceptFriend(requestDto);
        return ResponseEntity.ok("친구 요청을 수락하였습니다.");
    }

    // 친구 요청 거절
    @PostMapping("/reject")
    public ResponseEntity<String> rejectFriend(@RequestBody FriendRequestDto requestDto) {
        friendService.rejectFriend(requestDto);
        return ResponseEntity.ok("친구 요청을 거절하였습니다.");
    }

    // 친구 차단
    @PostMapping("/block")
    public ResponseEntity<String> blockFriend(@RequestBody FriendRequestDto requestDto) {
        friendService.blockFriend(requestDto);
        return ResponseEntity.ok("친구를 차단하였습니다.");
    }

    // 친구 차단 해제
    @PostMapping("/unblock")
    public ResponseEntity<String> unblockFriend(@RequestBody FriendRequestDto requestDto) {
        friendService.unblockFriend(requestDto);
        return ResponseEntity.ok("친구 차단을 해제하였습니다.");
    }

    // 친구 삭제
    @PostMapping("/delete")
    public ResponseEntity<String> deleteFriend(@RequestBody FriendRequestDto requestDto) {
        friendService.deleteFriend(requestDto);
        return ResponseEntity.ok("친구를 삭제하였습니다.");
    }

    // 친구 목록 조회
    @GetMapping("/list/{userId}")
    public ResponseEntity<List<FriendResponseDto>> getFriendList(@PathVariable String userId) {
        List<FriendResponseDto> friends = friendService.getFriendList(userId);
        return ResponseEntity.ok(friends);
    }

    // 친구 좋아요
    @PostMapping("/like")
    public ResponseEntity<String> likeFriend(@RequestBody FriendRequestDto requestDto) {
        friendService.likeFriend(requestDto);
        return ResponseEntity.ok("친구에게 좋아요를 눌렀습니다.");
    }

    // 친구 좋아요 취소
    @PostMapping("/unlike")
    public ResponseEntity<String> unlikeFriend(@RequestBody FriendRequestDto requestDto) {
        friendService.unlikeFriend(requestDto);
        return ResponseEntity.ok("친구에 대한 좋아요를 취소하였습니다.");
    }
}