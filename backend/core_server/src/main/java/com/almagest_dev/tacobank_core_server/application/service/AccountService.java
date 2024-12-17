package com.almagest_dev.tacobank_core_server.application.service;

import com.almagest_dev.tacobank_core_server.domain.account.model.FavoriteAccount;
import com.almagest_dev.tacobank_core_server.domain.account.model.MainAccount;
import com.almagest_dev.tacobank_core_server.domain.account.repository.FavoriteAccountRepository;
import com.almagest_dev.tacobank_core_server.domain.account.repository.MainAccountRepository;
import com.almagest_dev.tacobank_core_server.domain.friend.model.Friend;
import com.almagest_dev.tacobank_core_server.domain.friend.repository.FriendRepository;
import com.almagest_dev.tacobank_core_server.domain.member.model.Member;
import com.almagest_dev.tacobank_core_server.domain.member.repository.MemberRepository;
import com.almagest_dev.tacobank_core_server.domain.account.model.Account;
import com.almagest_dev.tacobank_core_server.domain.account.repository.AccountRepository;
import com.almagest_dev.tacobank_core_server.domain.transfer.model.Transfer;
import com.almagest_dev.tacobank_core_server.domain.transfer.repository.TransferRepository;

import com.almagest_dev.tacobank_core_server.infrastructure.external.testbed.client.TestbedApiClient;
import com.almagest_dev.tacobank_core_server.infrastructure.external.testbed.dto.AccountInfoDto;
import com.almagest_dev.tacobank_core_server.infrastructure.external.testbed.dto.IntegrateAccountApiRequestDto;
import com.almagest_dev.tacobank_core_server.infrastructure.external.testbed.dto.IntegrateAccountApiResponseDto;
import com.almagest_dev.tacobank_core_server.presentation.dto.account.Account2Dto;
import com.almagest_dev.tacobank_core_server.presentation.dto.account.AccountDto;
import com.almagest_dev.tacobank_core_server.presentation.dto.account.MainAccountRequestDto;
import com.almagest_dev.tacobank_core_server.presentation.dto.home.AccountResponseDto;
import com.almagest_dev.tacobank_core_server.presentation.dto.transfer.TransferOptionsResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService {

    private final MemberRepository memberRepository;
    private final AccountRepository accountRepository;
    private final MainAccountRepository mainAccountRepository;
    private final FavoriteAccountRepository favoriteAccountRepository;
    private final TransferRepository transferRepository;
    private final FriendRepository friendRepository;

    private final OrgCodeService orgCodeService;
    private final TestbedApiClient testbedApiClient;

    /**
     * 사용자 계좌 조회
     */
    @Transactional
    public List<AccountResponseDto> getUserAccountsOnly(Long memberId) {
        // 멤버 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("멤버를 찾을 수 없습니다."));

        String userFinanceId = member.getUserFinanceId();
        String userName = member.getName();
        IntegrateAccountApiResponseDto accountResponseDto;

        if (userFinanceId == null || userFinanceId.isEmpty()) {
            // 최초 요청 - memberId 기반으로 계좌 조회
            accountResponseDto = fetchAccountsFromApi(member.getId().toString(), userName);
            member.setUserFinanceId(accountResponseDto.getUserFinanceId());
            memberRepository.save(member);
        } else {
            // 이후 요청 - userFinanceId 기반으로 계좌 조회
            accountResponseDto = fetchAccountsFromApi(userFinanceId, userName);
        }

        // 계좌 정보 저장 (최초 요청 시)
        if (accountRepository.countByMember(member) == 0) {
            saveAccounts(accountResponseDto.getResList(), member);
        }

        // 계좌 정보 리스트 응답 생성
        return accountResponseDto.getResList().stream()
                .map(accountInfo -> {
                    AccountResponseDto accountDto = new AccountResponseDto();
                    accountDto.setAccountId(accountRepository.findByAccountNum(accountInfo.getAccountNum())
                            .map(Account::getId).orElse(null));
                    accountDto.setAccountName(accountInfo.getProductName());
                    accountDto.setBalance(Double.parseDouble(accountInfo.getBalanceAmt()));
                    accountDto.setBankCode(accountInfo.getBankCodeStd());
                    String bankName = orgCodeService.getBankNameByCode(accountInfo.getBankCodeStd());
                    accountDto.setBankName(bankName);
                    accountDto.setAccountNum(accountInfo.getAccountNum());
                    accountDto.setAccountHolder(accountInfo.getAccountHolder());
                    return accountDto;
                })
                .collect(Collectors.toList());
    }

    private IntegrateAccountApiResponseDto fetchAccountsFromApi(String userFinanceId, String userName) {
        IntegrateAccountApiRequestDto requestDto = new IntegrateAccountApiRequestDto();
        requestDto.setUserFinanceId(userFinanceId);
        requestDto.setUserName(userName);
        requestDto.setInquiryBankType("A");

        return testbedApiClient.requestApi(requestDto, "/openbank/accounts", IntegrateAccountApiResponseDto.class);
    }

    private void saveAccounts(List<AccountInfoDto> accountInfoList, Member member) {
        List<Account> accounts = accountInfoList.stream().map(accountInfo -> {
            Account account = new Account();
            account.saveMember(member);
            account.saveAccountNum(accountInfo.getAccountNum());
            account.saveAccountHolderName(accountInfo.getAccountHolder());
            account.saveBankCode(accountInfo.getBankCodeStd());
            account.saveFintechUseNum(accountInfo.getFintechUseNum());
            account.saveAccountName(accountInfo.getProductName());
            account.saveVerified();
            return account;
        }).collect(Collectors.toList());
        accountRepository.saveAll(accounts);
    }



    /**
     * 메인 계좌 설정
     */
    @Transactional
    public void setMainAccount(MainAccountRequestDto requestDto) {
        //Member 조회
        Member member = memberRepository.findById(requestDto.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        //Account 조회 및 검증
        Account account = accountRepository.findByIdAndMember(requestDto.getAccountId(), member)
                .orElseThrow(() -> new IllegalArgumentException("해당 계좌가 존재하지 않거나 회원의 계좌가 아닙니다."));

        //새로운 메인 계좌 저장
        MainAccount mainAccount = new MainAccount();
        mainAccount.saveMember(member);
        mainAccount.saveAccount(account);
        mainAccountRepository.save(mainAccount);
    }

    /**
     * 메인 계좌 수정
     */
    @Transactional
    public void updateMainAccount(MainAccountRequestDto requestDto) {
        // 기존 MainAccount 조회
        MainAccount mainAccount = mainAccountRepository.findByMemberId(requestDto.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("메인 계좌가 존재하지 않습니다."));

        // 계좌 변경
        Account account = accountRepository.findByIdAndMember(requestDto.getAccountId(), mainAccount.getMember())
                .orElseThrow(() -> new IllegalArgumentException("해당 계좌가 존재하지 않거나 회원의 계좌가 아닙니다."));

        // 기존 엔티티 업데이트
        mainAccount.saveAccount(account);
        mainAccountRepository.save(mainAccount);
    }


    /**
     * 즐겨찾기, 최근 이체, 친구 메인 계좌 조회
     */
    public TransferOptionsResponseDto getTransferOptions(Long memberId) {
        Member member = memberRepository.findByIdAndDeleted(memberId, "N")
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 즐겨찾기 계좌 (모두 조회)
        List<FavoriteAccount> favoriteAccounts = favoriteAccountRepository.findAllByMember(member);
        List<Account2Dto> favoriteAccountDtos = favoriteAccounts.stream()
                .map(account -> new Account2Dto(
                        account.getAccountHolderName(),
                        account.getAccountNum(),
                        account.getBankCode()
                ))
                .toList();

        // 타코뱅크 서비스에서 최근 이체한 내역 5개 조회
        Pageable pageable = PageRequest.of(0, 5); // 최근 5개
        List<Transfer> recentTransfers = transferRepository.findTop5DistinctByMemberIdAndStatus(memberId, "S", pageable);
        List<Account2Dto> recentAccountDtos = recentTransfers.stream()
                .map(transfer -> new Account2Dto(
                        transfer.getReceiverAccountHolder(),
                        transfer.getReceiverAccountNum(),
                        transfer.getReceiverBankCode()
                ))
                .toList();

        // 친구 메인 계좌 (모두 조회)
        List<Friend> friends = friendRepository.findByRequesterIdAndStatus(memberId, "ACC");
        List<Account2Dto> friendsAccountDtos = new ArrayList<>();
        for (Friend friend : friends) {
            Long friendId = friend.getReceiverId();
            // Get Main Account
            mainAccountRepository.findByMemberId(friendId) // 친구의 메인 계좌 조회
                    .ifPresent(mainAccount -> {
                        Long accountId = mainAccount.getAccount().getId();
                        accountRepository.findById(accountId) // 계좌 정보 조회
                                .ifPresent(account -> {
                                    friendsAccountDtos.add(new Account2Dto( // DTO에 매핑
                                            account.getAccountHolderName(),
                                            account.getAccountNum(),
                                            account.getBankCode()
                                    ));
                                });
                    });

        }

        return new TransferOptionsResponseDto(favoriteAccountDtos, recentAccountDtos, friendsAccountDtos);
    }
}