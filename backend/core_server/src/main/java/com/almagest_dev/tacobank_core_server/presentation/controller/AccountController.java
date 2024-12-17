package com.almagest_dev.tacobank_core_server.presentation.controller;

import com.almagest_dev.tacobank_core_server.application.service.AccountService;
import com.almagest_dev.tacobank_core_server.application.service.FavoriteAccountService;
import com.almagest_dev.tacobank_core_server.common.dto.CoreResponseDto;
import com.almagest_dev.tacobank_core_server.presentation.dto.account.FavoriteAccountRequestDto;
import com.almagest_dev.tacobank_core_server.presentation.dto.account.FavoriteAccountResponseDto;
import com.almagest_dev.tacobank_core_server.presentation.dto.account.MainAccountRequestDto;
import com.almagest_dev.tacobank_core_server.presentation.dto.home.AccountMemberReponseDto;
import com.almagest_dev.tacobank_core_server.presentation.dto.home.AccountResponseDto;
import com.almagest_dev.tacobank_core_server.presentation.dto.transfer.TransferOptionsResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/taco/core/accounts")
public class AccountController {

    private final AccountService accountService;
    private final FavoriteAccountService favoriteAccountService;

    public AccountController(AccountService accountService, FavoriteAccountService favoriteAccountService) {
        this.accountService = accountService;
        this.favoriteAccountService = favoriteAccountService;
    }


    // 메인 계좌 설정
    @PostMapping("/set-main")
    public ResponseEntity<CoreResponseDto<?>> setMainAccount(@RequestBody MainAccountRequestDto requestDto) {
        accountService.setMainAccount(requestDto);
        return ResponseEntity.ok(
                new CoreResponseDto<>(
                        "SUCCESS",
                        "메인 계좌가 설정되었습니다.",
                        null
                )
        );
    }

    // 메인 계좌 수정
    @PutMapping("/update-main")
    public ResponseEntity<CoreResponseDto<String>> updateMainAccount(@RequestBody MainAccountRequestDto requestDto) {
        accountService.updateMainAccount(requestDto);
        return ResponseEntity.ok(
                new CoreResponseDto<>("SUCCESS", "메인 계좌가 수정되었습니다.", null)
        );
    }

    // 즐겨찾기 계좌 설정
    @PostMapping("/favorite-account")
    public ResponseEntity<CoreResponseDto<String>> setAndRetrieveFavoriteAccount(@RequestBody FavoriteAccountRequestDto requestDto) {
        favoriteAccountService.setAndRetrieveFavoriteAccount(requestDto);
        return ResponseEntity.ok(
                new CoreResponseDto<>("SUCCESS", "즐겨찾기 계좌가 설정되었습니다.", null)
        );
    }

    // 즐겨찾기 계좌 취소
    @DeleteMapping("/favorite-account")
    public ResponseEntity<CoreResponseDto<String>> cancelFavoriteAccount(@RequestBody FavoriteAccountRequestDto requestDto) {
        favoriteAccountService.cancelFavoriteAccount(requestDto);
        return ResponseEntity.ok(
                new CoreResponseDto<>("SUCCESS", "즐겨찾기 계좌가 성공적으로 취소되었습니다.", null)
        );
    }

    // 즐겨찾기 계좌 목록 조회
    @GetMapping("/favorite-account/list/{memberId}")
    public ResponseEntity<CoreResponseDto<List<FavoriteAccountResponseDto>>> getFavoriteAccounts(
            @PathVariable Long memberId) {
        List<FavoriteAccountResponseDto> responseDtoList = favoriteAccountService.getFavoriteAccounts(memberId);
        return ResponseEntity.ok(
                new CoreResponseDto<>("SUCCESS", "즐겨찾기 계좌 목록 조회 성공", responseDtoList)
        );
    }

    // 즐겨찾기, 최근 이체, 친구 메인 계좌 조회
    @GetMapping("/transfer-options/{memberId}")
    public ResponseEntity<?> getTransferOptions(@PathVariable Long memberId) {
        TransferOptionsResponseDto response = accountService.getTransferOptions(memberId);
        return ResponseEntity.ok(new CoreResponseDto<>("SUCCESS", "계좌 조회 성공", response));
    }

//    @PostMapping("/list")
//    public ResponseEntity<?> getUserAccountsOnly(@RequestBody Map<String, Long> request) {
//        Long memberId = request.get("memberId");
//        List<AccountResponseDto> accounts = accountService.getUserAccountsOnly(memberId);
//        return ResponseEntity.ok(new CoreResponseDto<>("SUCCESS", "계좌 조회 성공", accounts));
//    }
    @GetMapping("/list/{memberId}")
    public ResponseEntity<?> getUserAccountsOnly(@PathVariable Long memberId) {
        List<AccountResponseDto> accounts = accountService.getUserAccountsOnly(memberId);
        return ResponseEntity.ok(new CoreResponseDto<>(
                "SUCCESS",
                "계좌 조회 성공",
                accounts
        ));
    }


}