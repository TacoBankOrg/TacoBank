package com.almagest_dev.tacobank_core_server.presentation.controller;

import com.almagest_dev.tacobank_core_server.application.service.HomeService;
import com.almagest_dev.tacobank_core_server.presentation.dto.home.AccountMemberReponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/taco/core/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    /**
     * 사용자 계좌 및 거래 내역 조회
     */
    @GetMapping
    public ResponseEntity<AccountMemberReponseDto> getUserAccounts() {
        AccountMemberReponseDto responseDto = homeService.getMemberHome();
        return ResponseEntity.ok(responseDto);
    }


}
