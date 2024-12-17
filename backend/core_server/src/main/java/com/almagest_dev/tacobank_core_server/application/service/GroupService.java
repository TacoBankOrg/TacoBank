package com.almagest_dev.tacobank_core_server.application.service;

import com.almagest_dev.tacobank_core_server.domain.friend.model.Friend;
import com.almagest_dev.tacobank_core_server.domain.friend.repository.FriendRepository;
import com.almagest_dev.tacobank_core_server.domain.group.model.Group;
import com.almagest_dev.tacobank_core_server.domain.group.model.GroupMember;
import com.almagest_dev.tacobank_core_server.domain.group.repository.GroupMemberRepository;
import com.almagest_dev.tacobank_core_server.domain.group.repository.GroupRepository;
import com.almagest_dev.tacobank_core_server.domain.member.model.Member;
import com.almagest_dev.tacobank_core_server.domain.member.repository.MemberRepository;
import com.almagest_dev.tacobank_core_server.presentation.dto.group.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final FriendRepository friendRepository;
    private final MemberRepository memberRepository;


    /**
     * 그룹장 확인 메서드
     */
    private void validateLeader(Long userId, Group group) {
        if (!group.getLeader().getId().equals(userId)) {
            throw new IllegalArgumentException("그룹장만 수행할 수 있는 작업입니다.");
        }
    }

    /**
     * 그룹 만들기
     */
    @Transactional
    public GroupResponseDto createGroup(Long userId, GroupRequestDto requestDto) {
        Member leader = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("그룹장을 찾을 수 없습니다."));

        // 그룹 이름 검증
        if (requestDto.getGroupName() == null || requestDto.getGroupName().trim().isEmpty()) {
            throw new IllegalArgumentException("그룹 이름 입력을 안했습니다.");
        }

        // 그룹 생성
        Group group = Group.createGroup(
                leader,
                requestDto.getGroupName(),
                "Y", // 활성화 상태
                "Y"  // 커스텀 여부
        );

        final Group savedGroup = groupRepository.save(group);

        // 그룹 멤버로 리더 추가
        GroupMember groupMember = new GroupMember();
        groupMember.savePayGroup(savedGroup);
        groupMember.saveMember(leader);
        groupMember.saveStatus("ACCEPTED"); // 리더는 자동으로 ACCEPTED 상태
        groupMemberRepository.save(groupMember);

        return new GroupResponseDto(
                savedGroup.getId(),
                savedGroup.getName(),
                savedGroup.getCustomized(),
                savedGroup.getActivated(),
                new ArrayList<>() // 멤버 목록은 초기화 상태로 반환
        );
    }


    /**
     * 그룹 비활성화 (활성화 여부를 'N'으로 변경)
     */
    @Transactional
    public void deactivateGroup(Long userId, Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));
        validateLeader(userId, group);
        group.unActivated("N");
        groupRepository.save(group);
    }

    /**
     * 그룹 멤버 초대 요청 보내기
     */
    public void inviteFriend(Long userId, Long groupId, Long friendId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));
        validateLeader(userId, group);

        // 초대 가능한 친구 ID 목록
        List<Long> inviteableFriendIds = getInviteableFriends(userId).stream()
                .map(friend -> (Long) friend.get("friendId")) // Map에서 "friendId"를 가져옴
                .toList();

        if (!inviteableFriendIds.contains(friendId)) {
            throw new IllegalArgumentException("초대하려는 사용자가 그룹장의 친구가 아닙니다.");
        }

        GroupMember existingMember = groupMemberRepository.findByPayGroupIdAndMemberId(groupId, friendId).orElse(null);

        if (existingMember != null) {
            if ("LEAVED".equals(existingMember.getStatus()) || "EXPELLED".equals(existingMember.getStatus()) || "REJECTED".equals(existingMember.getStatus())) {
                existingMember.saveStatus("INVITED");
                groupMemberRepository.save(existingMember);
            } else {
                throw new IllegalArgumentException("이미 그룹에 초대되었거나 수락한 멤버입니다.");
            }
        } else {
            GroupMember groupMember = new GroupMember();
            groupMember.savePayGroup(group);
            groupMember.saveMember(memberRepository.findById(friendId)
                    .orElseThrow(() -> new IllegalArgumentException("친구를 찾을 수 없습니다.")));
            groupMember.saveStatus("INVITED");
            groupMemberRepository.save(groupMember);
        }

        // 그룹 수정 날짜 업데이트
        group.updateGroup();
        groupRepository.save(group);
    }

    /**
     * 그룹 초대 가능한 친구 조회
     */
    public List<Map<String, Object>> getInviteableFriends(Long memberId) {
        List<Friend> friends = friendRepository.findByRequesterIdAndStatusOrReceiverIdAndStatus(
                memberId, "ACC", memberId, "ACC");

        // Set을 사용하여 중복 제거
        Set<Map<String, Object>> uniqueFriends = friends.stream()
                .map(friend -> {
                    Long friendId = friend.getRequesterId().equals(memberId)
                            ? friend.getReceiverId()
                            : friend.getRequesterId();
                    String friendName = memberRepository.findByIdAndDeleted(friendId,"N")
                            .map(Member::getName)
                            .orElse(null);
                    Map<String, Object> friendInfo = new HashMap<>();
                    friendInfo.put("friendId", friendId);
                    friendInfo.put("name", friendName);
                    return friendInfo;
                })
                .filter(friendInfo -> friendInfo.get("name") != null)
                .collect(Collectors.toSet());

        return new ArrayList<>(uniqueFriends);
    }

    /**
     * 그룹 멤버 추방하기
     */
    @Transactional
    public void expelMember(Long userId, Long groupId, Long memberId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));
        validateLeader(userId, group);

        GroupMember groupMember = groupMemberRepository.findByPayGroupIdAndMemberId(groupId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("멤버를 찾을 수 없습니다."));

        if (!"ACCEPTED".equals(groupMember.getStatus())) {
            throw new IllegalArgumentException("수락한 멤버만 추방할 수 있습니다.");
        }

        groupMember.saveStatus("EXPELLED");
        groupMemberRepository.save(groupMember);
    }

    /**
     * 그룹 요청 수락하기
     */
    @Transactional
    public void acceptInvitation(Long userId, Long groupId) {
        GroupMember groupMember = groupMemberRepository.findByPayGroupIdAndMemberId(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("초대를 찾을 수 없습니다."));

        if ("ACCEPTED".equals(groupMember.getStatus())) {
            throw new IllegalArgumentException("이미 초대를 수락한 상태입니다.");
        }
        if (!"INVITED".equals(groupMember.getStatus())) {
            throw new IllegalArgumentException("초대 상태가 아닙니다.");
        }

        groupMember.saveStatus("ACCEPTED");
        groupMemberRepository.save(groupMember);

        Group group = groupMember.getPayGroup();
        group.updateGroup();
        groupRepository.save(group);
    }

    /**
     * 그룹 거절하기
     */
    @Transactional
    public void rejectInvitation(Long userId, Long groupId) {
        GroupMember groupMember = groupMemberRepository.findByPayGroupIdAndMemberId(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("초대를 찾을 수 없습니다."));

        if (!"INVITED".equals(groupMember.getStatus())) {
            throw new IllegalArgumentException("초대 상태가 아닙니다.");
        }

        if ("ACCEPTED".equals(groupMember.getStatus())) {
            throw new IllegalArgumentException("이미 초대를 수락한 상태에서는 거절할 수 없습니다.");
        }

        groupMember.saveStatus("REJECTED");
        groupMemberRepository.save(groupMember);

        Group group = groupMember.getPayGroup();
        group.updateGroup();
        groupRepository.save(group);
    }

    /**
     * 그룹 떠나기(나가기)
     */
    @Transactional
    public void leaveGroup(Long userId, Long groupId) {
        GroupMember groupMember = groupMemberRepository.findByPayGroupIdAndMemberId(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("그룹 멤버를 찾을 수 없습니다."));

        if ("EXPELLED".equals(groupMember.getStatus())) {
            throw new IllegalArgumentException("추방된 멤버는 그룹을 나갈 수 없습니다.");
        }

        if (!"ACCEPTED".equals(groupMember.getStatus())) {
            throw new IllegalArgumentException("그룹 나가기가 불가능한 상태입니다.");
        }

        groupMember.saveStatus("LEAVED");
        groupMemberRepository.save(groupMember);

        Group group = groupMember.getPayGroup();
        group.updateGroup();
        groupRepository.save(group);
    }


    /**
     * 나의 그룹 조회
     */
    public List<MyGroupsResponseDto> getMyGroups(Long memberId) {
        // 1. 리더로서 속한 그룹 조회
        List<Group> leaderGroups = groupRepository.findByLeaderIdAndActivated(memberId, "Y");

        // 2. 그룹 멤버로 속한 그룹 조회 (ACCEPTED 상태)
        List<Group> memberGroups = groupRepository.findGroupsByAcceptedMember(memberId);

        // 3. 두 그룹 목록 합치기 및 중복 제거
        List<Group> allGroups = Stream.concat(leaderGroups.stream(), memberGroups.stream())
                .distinct() // 중복 제거
                .filter(group -> "Y".equals(group.getActivated())) // activated = "Y" 필터링
                .filter(group -> !"N".equals(group.getCustomized())) // customized = "N" 제외
                .collect(Collectors.toList());

        // 4. 그룹 데이터를 Dto로 변환
        return allGroups.stream().map(group -> {
            MyGroupsResponseDto response = new MyGroupsResponseDto();
            response.setGroupId(group.getId());
            response.setGroupName(group.getName());
            response.setCustomized(group.getCustomized());
            response.setActivated(group.getActivated());
            response.setLeaderId(group.getLeader().getId());

            // 리더 이름 조회
            Member leader = memberRepository.findByIdAndDeleted(group.getLeader().getId(),"N")
                    .orElse(null);
            response.setLeaderName(leader.getName());

            // 그룹 멤버 정보 조회 및 필터링
            List<MyGroupsResponseDto.MemberInfo> members = group.getPayGroups().stream()
                    .filter(pg -> "ACCEPTED".equals(pg.getStatus())) // ACCEPTED 상태인 멤버만 필터링
                    .filter(pg -> "N".equals(pg.getMember().getDeleted()))
                    .map(pg -> {
                        MyGroupsResponseDto.MemberInfo memberInfo = new MyGroupsResponseDto.MemberInfo();
                        memberInfo.setGroupId(pg.getPayGroup().getId());
                        memberInfo.setMemberId(pg.getMember().getId());
                        memberInfo.setStatus(pg.getStatus());

                        // 멤버 이름 조회
                        Member memberEntity = memberRepository.findByIdAndDeleted(pg.getMember().getId(),"N")
                                .orElse(null);
                        memberInfo.setMemberName(memberEntity.getName());
                        return memberInfo;
                    })
                    .filter(memberInfo -> memberInfo.getMemberName() != null)
                    .collect(Collectors.toList());

            response.setMembers(members);
            return response;
        }).collect(Collectors.toList());
    }

    /**
     * 그룹 초대 받은 요청 목록 조회
     */
    public List<GroupMemberResponseDto> getPendingInvitations(Long userId) {
        List<GroupMember> pendingInvitations = groupMemberRepository.findByMemberIdAndStatus(userId, "INVITED");

        return pendingInvitations.stream()
                .map(member -> new GroupMemberResponseDto(
                        member.getPayGroup().getId(),
                        member.getMember().getId(),
                        member.getStatus(),
                        member.getPayGroup().getName()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 그룹 검색하기
     */
    @Transactional
    public GroupSearchResponseDto searchGroupByName(String groupName) {
        // 그룹 이름으로 그룹을 조회
        Group group = groupRepository.findByNameAndActivated(groupName, "Y")
                .orElseThrow(() -> new IllegalArgumentException("해당 이름의 그룹을 찾을 수 없습니다."));

        // 그룹에 속한 멤버 조회 (ACCEPTED 상태와 deleted가 "N"인 경우만 필터링)
        List<GroupMemberSearchResponseDto> members = groupMemberRepository.findByPayGroupId(group.getId()).stream()
                .filter(groupMember -> "ACCEPTED".equals(groupMember.getStatus())) // ACCEPTED 상태 필터링
                .map(groupMember -> {
                    // 멤버 정보 조회 시 deleted 필터링 추가
                    Member member = memberRepository.findByIdAndDeleted(groupMember.getMember().getId(),"N")
                            .orElse(null);

                    return new GroupMemberSearchResponseDto(
                            member.getId(),
                            member.getName(),
                            groupMember.getStatus()
                    );
                })
                .filter(memberInfo -> memberInfo.getMemberName() != null)
                .collect(Collectors.toList());


        // 그룹 정보와 멤버 정보를 DTO로 반환
        return new GroupSearchResponseDto(
                group.getId(),
                group.getName(),
                members
        );
    }

    /**
     * 내가 리더로 설정된 그룹 조회
     */
    public List<MyGroupsResponseDto> getGroupsWhereLeader(Long leaderId) {
        // 리더로 설정된 그룹만 조회
        List<Group> leaderGroups = groupRepository.findByLeaderIdAndActivated(leaderId, "Y");

        // 그룹 데이터를 DTO로 변환
        return leaderGroups.stream()
                .map(group -> {
                    MyGroupsResponseDto response = new MyGroupsResponseDto();
                    response.setGroupId(group.getId());
                    response.setGroupName(group.getName());
                    response.setCustomized(group.getCustomized());
                    response.setActivated(group.getActivated());
                    response.setLeaderId(group.getLeader().getId());

                    // 리더 이름 조회
                    Member leader = memberRepository.findByIdAndDeleted(group.getLeader().getId(), "N")
                            .orElseThrow(() -> new IllegalArgumentException("해당 리더를 찾을 수 없습니다. (리더 ID: " + group.getLeader().getId() + ")"));
                    response.setLeaderName(leader.getName());

                    // 그룹 멤버 정보 조회 및 필터링
                    List<MyGroupsResponseDto.MemberInfo> members = group.getPayGroups().stream()
                            .filter(pg -> "ACCEPTED".equals(pg.getStatus())) // ACCEPTED 상태인 멤버만 필터링
                            .map(pg -> {
                                MyGroupsResponseDto.MemberInfo memberInfo = new MyGroupsResponseDto.MemberInfo();
                                memberInfo.setGroupId(pg.getPayGroup().getId());
                                memberInfo.setMemberId(pg.getMember().getId());
                                memberInfo.setStatus(pg.getStatus());

                                // 멤버 상태 확인 및 이름 조회
                                Member memberEntity = memberRepository.findByIdAndDeleted(pg.getMember().getId(), "N")
                                        .orElse(null);

                                if (memberEntity == null || "Y".equals(memberEntity.getDeleted())) {
                                    // 상태가 Y(비활성화)이면 null 반환 -> 필터링됨
                                    return null;
                                }

                                memberInfo.setMemberName(memberEntity.getName());
                                return memberInfo;
                            })
                            .filter(Objects::nonNull) // null 값 필터링
                            .collect(Collectors.toList());

                    response.setMembers(members);
                    return response;
                })
                .filter(response -> response.getLeaderName() != null) // 리더가 비활성화된 그룹은 제외
                .collect(Collectors.toList());
    }


}