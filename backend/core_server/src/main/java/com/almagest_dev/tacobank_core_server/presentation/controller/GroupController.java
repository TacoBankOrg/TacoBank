package com.almagest_dev.tacobank_core_server.presentation.controller;

import com.almagest_dev.tacobank_core_server.application.service.GroupService;
import com.almagest_dev.tacobank_core_server.common.dto.CoreResponseDto;
import com.almagest_dev.tacobank_core_server.presentation.dto.group.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/taco/core/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    // 그룹 생성
    @PostMapping
    public ResponseEntity<CoreResponseDto<GroupResponseDto>> createGroup(@RequestBody GroupRequestDto requestDto) {
        GroupResponseDto groupResponse = groupService.createGroup(requestDto.getLeaderId(), requestDto);
        return ResponseEntity.ok(
                new CoreResponseDto<>("SUCCESS", "그룹 생성이 완료되었습니다.", groupResponse)
        );
    }

    /**
     * 그룹 비활성화 (그룹장만 가능)
     */
    @PostMapping("/deactivate")
    public ResponseEntity<CoreResponseDto<String>> deactivateGroup(@RequestBody Map<String, Long> request) {
        Long userId = request.get("memberId");
        Long groupId = request.get("groupId");
        groupService.deactivateGroup(userId, groupId);
        return ResponseEntity.ok(
                new CoreResponseDto<>(
                        "SUCCESS",
                        "그룹이 비활성화되었습니다.",
                        null
                )
        );
    }


    // 친구 초대 (그룹장만 가능)
    @PostMapping("/invite")
    public ResponseEntity<CoreResponseDto<String>> inviteFriend(@RequestBody Map<String, Long> request) {
        Long userId = request.get("memberId");
        Long groupId = request.get("groupId");
        Long friendId = request.get("friendId");
        groupService.inviteFriend(userId, groupId, friendId);
        return ResponseEntity.ok(
                new CoreResponseDto<>("SUCCESS", "초대 신청이 완료되었습니다.", null)
        );
    }

    // 그룹에 초대 가능한 친구 조회
    @GetMapping("/{memberId}/friends/list")
    public ResponseEntity<CoreResponseDto<List<Map<String, Object>>>> getInviteableFriends(@PathVariable Long memberId) {
        List<Map<String, Object>> inviteableFriends = groupService.getInviteableFriends(memberId);
        return ResponseEntity.ok(
                new CoreResponseDto<>("SUCCESS", "초대 가능한 친구 목록 조회 성공", inviteableFriends)
        );
    }

    // 멤버 추방 (그룹장만 가능)
    @PostMapping("/expel")
    public ResponseEntity<CoreResponseDto<String>> expelMember(@RequestBody Map<String, Long> request) {
        Long userId = request.get("memberId");
        Long groupId = request.get("groupId");
        Long memberId = request.get("targetMemberId");
        groupService.expelMember(userId, groupId, memberId);
        return ResponseEntity.ok(
                new CoreResponseDto<>("SUCCESS", "멤버 추방이 완료되었습니다.", null)
        );
    }

    // 초대 수락 (멤버만 가능)
    @PostMapping("/accept")
    public ResponseEntity<CoreResponseDto<String>> acceptInvitation(@RequestBody Map<String, Long> request) {
        Long userId = request.get("memberId");
        Long groupId = request.get("groupId");
        groupService.acceptInvitation(userId, groupId);
        return ResponseEntity.ok(
                new CoreResponseDto<>("SUCCESS", "그룹 승인이 완료되었습니다.", null)
        );
    }

    // 초대 거절 (멤버만 가능)
    @PostMapping("/reject")
    public ResponseEntity<CoreResponseDto<String>> rejectInvitation(@RequestBody Map<String, Long> request) {
        Long userId = request.get("memberId");
        Long groupId = request.get("groupId");
        groupService.rejectInvitation(userId, groupId);
        return ResponseEntity.ok(
                new CoreResponseDto<>("SUCCESS", "그룹 요청 거절이 완료되었습니다.", null)
        );
    }

    // 그룹 나가기 (멤버만 가능)
    @PostMapping("/leave")
    public ResponseEntity<CoreResponseDto<String>> leaveGroup(@RequestBody Map<String, Long> request) {
        Long userId = request.get("memberId");
        Long groupId = request.get("groupId");
        groupService.leaveGroup(userId, groupId);
        return ResponseEntity.ok(
                new CoreResponseDto<>("SUCCESS", "그룹 나가기가 완료되었습니다.", null)
        );
    }

    // 내가 속한 그룹 목록 조회
    @GetMapping("/my-group/list/{memberId}")
    public ResponseEntity<CoreResponseDto<List<MyGroupsResponseDto>>> getMyGroups(@PathVariable Long memberId) {
        List<MyGroupsResponseDto> myGroups = groupService.getMyGroups(memberId);
        return ResponseEntity.ok(
                new CoreResponseDto<>("SUCCESS", "내가 속한 그룹 목록 조회 성공", myGroups)
        );
    }

    // 초대 대기 목록 조회 (초대를 수락하지 않은 목록)
    @GetMapping("/invitations/{memberId}")
    public ResponseEntity<CoreResponseDto<List<GroupMemberResponseDto>>> getPendingInvitations(@PathVariable Long memberId) {
        List<GroupMemberResponseDto> pendingInvitations = groupService.getPendingInvitations(memberId);
        return ResponseEntity.ok(
                new CoreResponseDto<>("SUCCESS", "초대 대기 목록 조회 성공", pendingInvitations)
        );
    }

    // 그룹 검색하기
    @GetMapping("/search/{groupName}")
    public ResponseEntity<CoreResponseDto<GroupSearchResponseDto>> searchGroupByName(@PathVariable String groupName) {
        GroupSearchResponseDto groupInfo = groupService.searchGroupByName(groupName);
        return ResponseEntity.ok(
                new CoreResponseDto<>("SUCCESS", "그룹 검색 성공", groupInfo)
        );
    }

    // 내가 리더인 그룹 목록 조회
    @GetMapping("/leader-group/list/{memberId}")
    public ResponseEntity<CoreResponseDto<List<MyGroupsResponseDto>>> getLeaderGroups(@PathVariable Long memberId) {
        List<MyGroupsResponseDto> leaderGroups = groupService.getGroupsWhereLeader(memberId);
        return ResponseEntity.ok(
                new CoreResponseDto<>("SUCCESS", "내가 리더인 그룹 목록 조회 성공", leaderGroups)
        );
    }

}