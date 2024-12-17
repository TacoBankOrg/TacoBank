package com.almagest_dev.tacobank_core_server.presentation.dto.receipt;

import lombok.Data;

import java.util.List;

@Data
public class ProductMemberDetails {
    private long productId;             // 영수증 품목 ID
    private List<Long> productMembers; // 영수증 품목별 포함된 멤버 ID 리스트
}
