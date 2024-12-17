package com.almagest_dev.tacobank_core_server.application.service;


import com.almagest_dev.tacobank_core_server.domain.friend.model.Friend;
import com.almagest_dev.tacobank_core_server.domain.friend.repository.FriendRepository;
import com.almagest_dev.tacobank_core_server.domain.member.model.Member;
import com.almagest_dev.tacobank_core_server.domain.member.repository.MemberRepository;
import com.almagest_dev.tacobank_core_server.presentation.dto.friend.FriendRequestDto;
import com.almagest_dev.tacobank_core_server.presentation.dto.friend.FriendResponseDto;
import com.almagest_dev.tacobank_core_server.presentation.dto.friend.FriendResponseDto2;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;
    private final MemberRepository memberRepository;

    /**
     * 친구상태 관리 조건제약
     */
    private void checkActionPermission(Friend friend, String action, Long userId) {
        // 현재 사용자가 요청자(Requester) 또는 수신자(Receiver)인지 확인
        boolean isRequester = friend.getRequesterId().equals(userId);
        boolean isReceiver = friend.getReceiverId().equals(userId);

        switch (friend.getStatus()) {
            case "REQ":
                // REQ 상태에서는 추가 동작을 수행하지 않도록 제한
                throw new IllegalStateException("요청 중인 상태에서는 추가 동작을 수행할 수 없습니다.");

            case "REQ_RECEIVED":
                // REQ_RECEIVED 상태에서는 요청을 수락하거나 거절할 수 있도록 설정
                if (!action.equals("accept") && !action.equals("reject")) {
                    throw new IllegalStateException("요청 수신 상태에서는 수락과 거절만 가능합니다.");
                }
                break;

            case "ACC":
                // 수락 상태에서는 요청과 거절이 불가능하고, 차단, 차단 해제, 좋아요, 좋아요 취소, 삭제만 가능
                if (action.equals("request") || action.equals("reject") || action.equals("accept")) {
                    throw new IllegalStateException("수락 상태에서는 요청과 거절이 불가능합니다.");
                }
                break;

            case "REJ":
                // 거절 상태에서는 재요청만 가능
                if (!action.equals("request")) {
                    throw new IllegalStateException("거절 상태에서는 재요청만 가능합니다.");
                }
                break;

            case "BLOCKED":
                // 요청자가 unblock 작업을 수행할 수 있도록 설정
                if (!isRequester || !action.equals("unblock")) {
                    throw new IllegalStateException("차단 상태에서는 차단 해제만 가능합니다.");
                }
                break;

            case "BLOCKED_BY":
                if (action.equals("like") || action.equals("unlike")) {
                    throw new IllegalStateException("차단된 사용자는 좋아요 또는 좋아요 취소를 수행할 수 없습니다.");
                }
                if (isReceiver && (action.equals("block") || action.equals("unblock") || action.equals("request") ||
                        action.equals("accept") || action.equals("reject") || action.equals("delete"))) {
                    throw new IllegalStateException("해당 사용자에게 차단 당했습니다. 추가 동작이 불가합니다.");
                }
                break;

            case "NONE":
                // 차단 해제 상태에서는 재요청만 가능
                if (!action.equals("request")) {
                    throw new IllegalStateException("친구 해제 상태에서는 재요청만 가능합니다.");
                }
                break;


            default:
                throw new IllegalArgumentException("알 수 없는 상태입니다."+  friend.getStatus());
        }
    }

    /**
     * 자신이 자신에게 친구 관계 설정 불가
     */
    private void validateUserId(Long userId, Long requesterId) {
        if (!userId.equals(requesterId)) {
            throw new IllegalArgumentException("userId와 requesterId가 일치하지 않습니다.");
        }
    }

    /**
     * 친구 요청
     */
    @Transactional
    public void requestFriend(Long userId, FriendRequestDto requestDto) {

        validateUserId(userId, requestDto.getRequesterId());

        // 요청자와 수신자가 동일한 경우 예외 처리
        if (requestDto.getRequesterId().equals(requestDto.getReceiverId())) {
            throw new IllegalStateException("자신에게 친구 요청을 보낼 수 없습니다.");
        }

        Friend friend = friendRepository.findByRequesterIdAndReceiverId(
                requestDto.getRequesterId(), requestDto.getReceiverId()
        ).orElseGet(() -> new Friend(requestDto.getRequesterId(), requestDto.getReceiverId()));

        // 요청 중복 방지 예외 처리
        checkActionPermission(friend, "request", userId);

        // 기존 요청 상태 확인
        Friend reverseFriend = friendRepository.findByRequesterIdAndReceiverId(
                requestDto.getReceiverId(), requestDto.getRequesterId()
        ).orElse(null);

        if (reverseFriend != null) {
            if ("REQ".equals(reverseFriend.getStatus())) {
                throw new IllegalArgumentException("상대방이 이미 요청을 보낸 상태입니다.");
            }
            if ("REQ_RECEIVED".equals(reverseFriend.getStatus())) {
                throw new IllegalArgumentException("이미 요청을 한 상태입니다.");
            }

            // 상대방이 ACC 상태일 경우, 바로 친구 관계 설정
            if ("ACC".equals(reverseFriend.getStatus()) && "DEL".equals(friend.getStatus())) {
                friend.saveStatus("ACC");
                reverseFriend.saveStatus("ACC");
            }

            // 둘 다 NONE 상태일 경우, REQ - REQ_RECEIVED 상태로 설정
            else if ("NONE".equals(friend.getStatus()) && "NONE".equals(reverseFriend.getStatus())) {
                friend.saveStatus("REQ");
                reverseFriend.saveStatus("REQ_RECEIVED");
            }

            else if ("NONE".equals(friend.getStatus()) && "REJ".equals(reverseFriend.getStatus())) {
                friend.saveStatus("REQ");
                reverseFriend.saveStatus("REQ_RECEIVED");
            }

            else if ("REJ".equals(friend.getStatus()) && "NONE".equals(reverseFriend.getStatus())) {
                friend.saveStatus("REQ");
                reverseFriend.saveStatus("REQ_RECEIVED");
            }
        } else {
            // 반대 관계가 없는 경우, REQ - REQ_RECEIVED 상태로 초기화
            friend.saveStatus("REQ");
            reverseFriend = new Friend(requestDto.getReceiverId(), requestDto.getRequesterId());
            reverseFriend.saveStatus("REQ_RECEIVED");
        }

        // 저장
        friendRepository.save(friend);
        friendRepository.save(reverseFriend);// 반대 상태 저장

        friend.updateGroup();

    }

    /**
     * 친구 수락
     */
    @Transactional
    public void acceptFriend(Long userId, FriendRequestDto requestDto) {

        validateUserId(userId, requestDto.getRequesterId());

        Friend friend = friendRepository.findByRequesterIdAndReceiverId(
                requestDto.getRequesterId(), requestDto.getReceiverId()
        ).orElseThrow(() -> new IllegalArgumentException("친구 요청이 없습니다."));

        checkActionPermission(friend, "accept",userId);

        friend.saveStatus("ACC");
        friendRepository.save(friend);

        // 반대 관계 상태를 수락 상태로 설정
        Friend reverseFriend = friendRepository.findByRequesterIdAndReceiverId(
                requestDto.getReceiverId(), requestDto.getRequesterId()
        ).orElseGet(() -> new Friend(requestDto.getReceiverId(), requestDto.getRequesterId()));
        reverseFriend.saveStatus("ACC");
        friendRepository.save(reverseFriend);
        friend.updateGroup();
    }

    /**
     * 친구 거절
     */
    @Transactional
    public void rejectFriend(Long userId,FriendRequestDto requestDto) {

        validateUserId(userId, requestDto.getRequesterId());

        Friend friend = friendRepository.findByRequesterIdAndReceiverId(
                requestDto.getRequesterId(), requestDto.getReceiverId()
        ).orElseThrow(() -> new IllegalArgumentException("친구 요청이 없습니다."));

        checkActionPermission(friend, "reject",userId);

        friend.saveStatus("REJ");
        friendRepository.save(friend);

        // 반대 관계 상태를 NONE으로 설정하여 요청을 보낼 수 있게 초기화
        Friend reverseFriend = friendRepository.findByRequesterIdAndReceiverId(
                requestDto.getReceiverId(), requestDto.getRequesterId()
        ).orElseGet(() -> new Friend(requestDto.getReceiverId(), requestDto.getRequesterId()));
        reverseFriend.saveStatus("NONE");
        friendRepository.save(reverseFriend);
        friend.updateGroup();
    }

    /**
     * 친구 삭제
     */
    @Transactional
    public void deleteFriend(Long userId,FriendRequestDto requestDto) {
        validateUserId(userId, requestDto.getRequesterId());
        Friend friend = friendRepository.findByRequesterIdAndReceiverId(
                requestDto.getRequesterId(), requestDto.getReceiverId()
        ).orElseThrow(() -> new IllegalArgumentException("친구 관계가 존재하지 않습니다."));

        checkActionPermission(friend, "delete",userId);

        friend.saveStatus("DEL");
        friend.saveLiked("N");
        friendRepository.save(friend);

        // 반대 관계 상태를 NONE으로 설정
        Friend reverseFriend = friendRepository.findByRequesterIdAndReceiverId(
                requestDto.getReceiverId(), requestDto.getRequesterId()
        ).orElseGet(() -> new Friend(requestDto.getReceiverId(), requestDto.getRequesterId()));

        // 상대방의 상태가 DEL이라면 DEL로 유지, 그렇지 않으면 ACC로 설정
        if ("DEL".equals(reverseFriend.getStatus())) {
            reverseFriend.saveStatus("DEL");
        } else {
            reverseFriend.saveStatus("ACC");
        }
        friendRepository.save(reverseFriend);
        friend.updateGroup();
    }


    /**
     * 친구 차단
     */
    @Transactional
    public void blockFriend(Long userId,FriendRequestDto requestDto) {
        validateUserId(userId, requestDto.getRequesterId());

        Friend friend = friendRepository.findByRequesterIdAndReceiverId(
                requestDto.getRequesterId(), requestDto.getReceiverId()
        ).orElseGet(() -> new Friend(requestDto.getRequesterId(), requestDto.getReceiverId()));
        checkActionPermission(friend, "block",userId);
        friend.saveStatus("BLOCKED");
        friend.saveLiked("N");
        friendRepository.save(friend);

        Friend reverseFriend = friendRepository.findByRequesterIdAndReceiverId(
                requestDto.getReceiverId(), requestDto.getRequesterId()
        ).orElseGet(() -> new Friend(requestDto.getReceiverId(), requestDto.getRequesterId()));
        reverseFriend.saveStatus("BLOCKED_BY");
        reverseFriend.saveLiked("N");
        friendRepository.save(reverseFriend);
        friend.updateGroup();
    }

    /**
     * 친구 차단 해제
     */
    @Transactional
    public void unblockFriend(Long userId,FriendRequestDto requestDto) {
        validateUserId(userId, requestDto.getRequesterId());
        Friend friend = friendRepository.findByRequesterIdAndReceiverId(
                requestDto.getRequesterId(), requestDto.getReceiverId()
        ).orElseThrow(() -> new IllegalArgumentException("차단된 관계가 없습니다."));

        checkActionPermission(friend, "unblock",userId);

        if ("BLOCKED".equals(friend.getStatus()) && userId.equals(friend.getRequesterId())) {
            friend.saveStatus("NONE");
            friendRepository.save(friend);

            Friend reverseFriend = friendRepository.findByRequesterIdAndReceiverId(
                    requestDto.getReceiverId(), requestDto.getRequesterId()
            ).orElseThrow(() -> new IllegalArgumentException("차단된 관계가 없습니다."));
            reverseFriend.saveStatus("NONE");
            friendRepository.save(reverseFriend);
        } else {
            throw new IllegalArgumentException("차단한 사용자만 차단 해제를 할 수 있습니다.");
        }
        friend.updateGroup();
    }

    /**
     * 친구 좋아요
     */
    @Transactional
    public void likeFriend(Long userId, FriendRequestDto requestDto) {
        validateUserId(userId, requestDto.getRequesterId());
        Friend friend = friendRepository.findByRequesterIdAndReceiverId(
                requestDto.getRequesterId(), requestDto.getReceiverId()
        ).orElseThrow(() -> new IllegalArgumentException("친구 관계가 존재하지 않습니다."));

        // 좋아요 가능한 상태인지 확인
        checkActionPermission(friend, "like", userId);

        if ("Y".equals(friend.getLiked())) {
            throw new IllegalArgumentException("이미 좋아요를 누른 상태입니다.");
        }

        friend.saveLiked("Y"); // 좋아요 상태 설정
        friendRepository.save(friend);
        friend.updateGroup();
    }

    /**
     * 친구 좋아요 취소
     */
    @Transactional
    public void unlikeFriend(Long userId, FriendRequestDto requestDto) {
        validateUserId(userId, requestDto.getRequesterId());
        Friend friend = friendRepository.findByRequesterIdAndReceiverId(
                requestDto.getRequesterId(), requestDto.getReceiverId()
        ).orElseThrow(() -> new IllegalArgumentException("친구 관계가 존재하지 않습니다."));

        // 좋아요 취소 가능한 상태인지 확인
        checkActionPermission(friend, "unlike", userId);

        if ("N".equals(friend.getLiked())) {
            throw new IllegalArgumentException("이미 좋아요 취소 상태입니다.");
        }

        friend.saveLiked("N"); // 좋아요 취소 상태 설정
        friendRepository.save(friend);
        friend.updateGroup();
    }

    /**
     * 자신의 친구 목록 조회
     */
    public List<FriendResponseDto> getFriendList(Long requesterId) {
        List<Friend> friends = friendRepository.findByRequesterIdAndStatus(requesterId, "ACC");

        return friends.stream()
                .map(friend -> {
                    Long friendId = friend.getReceiverId(); // requester_id가 요청자로 있는 친구의 receiver_id 가져오기
                    String liked = friend.getLiked();
                    String friendName = memberRepository.findByIdAndDeleted(friendId,"N")
                            .map(Member::getName)
                            .orElse(null);
                    return new FriendResponseDto(friendId, friendName, liked);
                })
                .filter(dto -> dto.getFriendName() != null)
                .distinct() // 중복 제거
                .collect(Collectors.toList());
    }


    /**
     * 자신이 차단한 친구 목록 조회
     */
    public List<FriendResponseDto2> getBlockedFriends(Long requesterId) {
        List<Friend> blockedFriends = friendRepository.findByRequesterIdAndStatus(requesterId, "BLOCKED");

        return blockedFriends.stream()
                .map(friend -> {
                    Long friendId = friend.getReceiverId(); // 차단된 친구의 ID
                    String friendName = memberRepository.findByIdAndDeleted(friendId,"N")
                            .map(Member::getName)
                            .orElse(null);
                    return new FriendResponseDto2(friendId, friendName); // DTO 생성
                })
                .filter(dto -> dto.getFriendName() != null)
                .collect(Collectors.toList());
    }

    /**
     * 받은 친구 요청 조회
     */
    public List<FriendResponseDto2> getReceivedFriendRequests(Long requesterId) {
        // requesterId 기준으로 요청 상태가 "REQ_RECEIVED"인 데이터 조회
        List<Friend> receivedRequests = friendRepository.findByRequesterIdAndStatus(requesterId, "REQ_RECEIVED");

        return receivedRequests.stream()
                .map(friend -> {
                    Long friendId = friend.getReceiverId(); // 수신자 ID
                    String friendName = memberRepository.findByIdAndDeleted(friendId,"N")
                            .map(Member::getName)
                            .orElse(null);
                    return new FriendResponseDto2(friendId, friendName);
                })
                .filter(dto -> dto.getFriendName() != null)
                .collect(Collectors.toList());
    }

    /**
     * 자신이 삭제한 친구 목록 조회
     */
    public List<FriendResponseDto2> getDeletedFriends(Long requesterId) {
        // requesterId 기준으로 요청 상태가 "DEL"인 데이터 조회
        List<Friend> deletedFriends = friendRepository.findByRequesterIdAndStatus(requesterId, "DEL");

        return deletedFriends.stream()
                .map(friend -> {
                    Long friendId = friend.getReceiverId(); // 삭제된 친구의 ID
                    String friendName = memberRepository.findByIdAndDeleted(friendId, "N")
                            .map(Member::getName)
                            .orElse(null);
                    return new FriendResponseDto2(friendId, friendName); // DTO 생성
                })
                .filter(dto -> dto.getFriendName() != null)
                .collect(Collectors.toList());
    }

    /**
     * 친구 삭제 취소
     */
    @Transactional
    public void undoDeleteFriend(Long userId, FriendRequestDto requestDto) {
        validateUserId(userId, requestDto.getRequesterId());

        Friend friend = friendRepository.findByRequesterIdAndReceiverId(
                requestDto.getRequesterId(), requestDto.getReceiverId()
        ).orElseThrow(() -> new IllegalArgumentException("삭제된 친구 관계가 존재하지 않습니다."));

        // 요청자의 상태가 DEL인지 확인
        if (!"DEL".equals(friend.getStatus())) {
            throw new IllegalStateException("삭제된 상태에서만 삭제 취소를 할 수 있습니다.");
        }

        // 반대 관계의 상태 확인
        Friend reverseFriend = friendRepository.findByRequesterIdAndReceiverId(
                requestDto.getReceiverId(), requestDto.getRequesterId()
        ).orElseThrow(() -> new IllegalArgumentException("반대 관계가 존재하지 않습니다."));

        // 삭제 상태에서 삭제 취소 처리
        if ("DEL".equals(reverseFriend.getStatus())) {
            // 둘 다 DEL 상태라면 ACC로 복구
            friend.saveStatus("ACC");
            reverseFriend.saveStatus("DEL");
        } else if ("ACC".equals(reverseFriend.getStatus())) {
            // 상대방이 ACC 상태인 경우에도 ACC로 복구
            friend.saveStatus("ACC");
        } else {
            throw new IllegalStateException("현재 상태에서는 삭제 취소를 수행할 수 없습니다.");
        }

        // 변경된 상태 저장
        friendRepository.save(friend);
        friendRepository.save(reverseFriend);

        friend.updateGroup();
        reverseFriend.updateGroup();
    }

}