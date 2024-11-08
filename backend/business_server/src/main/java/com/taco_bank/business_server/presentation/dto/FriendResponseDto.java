package com.taco_bank.business_server.presentation.dto;

import com.taco_bank.business_server.domain.friend.model.Friend;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class FriendResponseDto {
    private Long id;
    private String requesterId;
    private String receiverId;
    private String status;
    private String liked;

    public FriendResponseDto(Friend friend) {
        this.id = friend.getId();
        this.requesterId = friend.getRequesterId();
        this.receiverId = friend.getReceiverId();
        this.status = friend.getStatus();
        this.liked = friend.getLiked();
    }

}
