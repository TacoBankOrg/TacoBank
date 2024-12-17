package com.almagest_dev.tacobank_core_server.presentation.dto.friend;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FriendRequestDto {
    private Long requesterId;
    private Long receiverId;


}