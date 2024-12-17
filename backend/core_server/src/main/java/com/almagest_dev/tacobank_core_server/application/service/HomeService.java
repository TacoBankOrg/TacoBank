package com.almagest_dev.tacobank_core_server.application.service;

import com.almagest_dev.tacobank_core_server.common.exception.MemberAuthException;
import com.almagest_dev.tacobank_core_server.domain.account.model.Account;
import com.almagest_dev.tacobank_core_server.domain.account.model.MainAccount;
import com.almagest_dev.tacobank_core_server.domain.account.repository.AccountRepository;
import com.almagest_dev.tacobank_core_server.domain.account.repository.MainAccountRepository;
import com.almagest_dev.tacobank_core_server.domain.member.model.Member;
import com.almagest_dev.tacobank_core_server.domain.member.repository.MemberRepository;
import com.almagest_dev.tacobank_core_server.infrastructure.external.testbed.client.TestbedApiClient;
import com.almagest_dev.tacobank_core_server.infrastructure.external.testbed.dto.*;
import com.almagest_dev.tacobank_core_server.presentation.dto.home.AccountMemberReponseDto;
import com.almagest_dev.tacobank_core_server.presentation.dto.home.AccountResponseDto;
import com.almagest_dev.tacobank_core_server.presentation.dto.home.TransactionResponseDto2;
import com.almagest_dev.tacobank_core_server.presentation.dto.transantion.TransactionListRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeService {

    private final MemberRepository memberRepository;
    private final AccountRepository accountRepository;
    private final MainAccountRepository mainAccountRepository;

    private final TestbedApiClient testbedApiClient;
    private final OrgCodeService orgCodeService;

    /**
     * 사용자 계좌 및 거래 내역 조회
     */
    @Transactional
    public AccountMemberReponseDto getMemberHome() {
        /**
         * 인증 정보로 멤버 조회
         */
        // 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new MemberAuthException("인증되지 않은 사용자입니다.", HttpStatus.UNAUTHORIZED);
        }

        // 인증 정보에서 멤버 ID 추출
        Long memberId = (Long) authentication.getDetails();
        log.info("HomeService::getMemberHome - memberId: {} ", memberId);

        // 멤버 조회
        Member member = memberRepository.findByIdAndDeleted(memberId, "N")
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        String userFinanceId = member.getUserFinanceId();
        String userName = member.getName();
        IntegrateAccountApiResponseDto accountResponseDto;

        if (userFinanceId == null || userFinanceId.isEmpty()) {
            // 최초 요청 - memberId 기반으로 계좌 조회
            accountResponseDto = fetchAccountsFromApi(member.getId().toString(), userName);
            member.setUserFinanceId(accountResponseDto.getUserFinanceId());
            member.changeMydataLinked("Y");
            memberRepository.save(member);
        } else {
            // 이후 요청 - userFinanceId 기반으로 계좌 조회
            accountResponseDto = fetchAccountsFromApi(userFinanceId, userName);
        }

        // 계좌 정보 저장 (최초 요청 시)
        // @TODO 계좌 정보 저장 로직 수정 - 새로운 계좌만 추가
        if (accountRepository.countByMember(member) == 0) {
            saveAccounts(accountResponseDto.getResList(), member);
        }

        // 현재 시점 및 포맷팅 설정
        String fromDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String toDate = LocalDateTime.now().plusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String tranDtime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        // 계좌 및 거래 내역 응답 생성
        return mapToAccountMemberResponseDto(member, accountResponseDto, fromDate, toDate, tranDtime);
    }

    /**
     * 테스트베드 요청: 오픈뱅킹 계좌 연동(조회)
     */
    private IntegrateAccountApiResponseDto fetchAccountsFromApi(String userFinanceId, String userName) {
        IntegrateAccountApiRequestDto requestDto = new IntegrateAccountApiRequestDto();
        requestDto.setUserFinanceId(userFinanceId);
        requestDto.setUserName(userName);
        requestDto.setInquiryBankType("A");

        return testbedApiClient.requestApi(requestDto, "/openbank/accounts", IntegrateAccountApiResponseDto.class);
    }

    /**
     * 사용자 Account 조회 결과 저장
     */
    private void saveAccounts(List<AccountInfoDto> accountInfoList, Member member) {
        List<Account> accounts = accountInfoList.stream().map(accountInfo -> {
            Account account = new Account();
            account.saveMember(member);
            account.saveAccountNum(accountInfo.getAccountNum());
            account.saveAccountHolderName(accountInfo.getAccountHolder());
            account.saveBankCode(accountInfo.getBankCodeStd());
            account.saveFintechUseNum(accountInfo.getFintechUseNum());
            account.saveVerified();
            account.saveAccountName(accountInfo.getProductName());

            return account;
        }).collect(Collectors.toList());
        accountRepository.saveAll(accounts);
    }

    /**
     * 홈 화면 응답 데이터로 변환/매핑
     */
    private AccountMemberReponseDto mapToAccountMemberResponseDto(Member member, IntegrateAccountApiResponseDto accountResponseDto, String fromDate, String toDate, String tranDtime) {
        AccountMemberReponseDto response = new AccountMemberReponseDto();
        response.setMemberId(member.getId());
        response.setEmail(member.getEmail());
        response.setName(member.getName());
        response.setTel(member.getTel());
        response.setMydataLinked(member.getMydataLinked());

        boolean isPinSet = member.getTransferPin() != null;
        response.setPinSet(isPinSet);

        // 잔액이 가장 많은 계좌 정보를 위한 변수
        long highestBalanceAccountId = 0L;
        double maxBalance = 0.0;

        // 계좌 리스트 생성
        List<AccountResponseDto> accountList = new ArrayList<>();

        // 계좌 정보 반복 처리
        for (AccountInfoDto accountInfo : accountResponseDto.getResList()) {
            // AccountResponseDto 객체 생성
            AccountResponseDto accountDto = new AccountResponseDto();
            Long accountId = accountRepository.findByAccountNum(accountInfo.getAccountNum())
                    .map(Account::getId)
                    .orElse(null);
            accountDto.setAccountId(accountId);
            accountDto.setAccountName(accountInfo.getProductName());
            accountDto.setBankCode(accountInfo.getBankCodeStd());

            // 은행 이름 설정
            String bankName = orgCodeService.getBankNameByCode(accountInfo.getBankCodeStd());
            accountDto.setBankName(bankName);
            accountDto.setAccountNum(accountInfo.getAccountNum());
            accountDto.setAccountHolder(accountInfo.getAccountHolder());

            // 잔액 설정
            double balance = Double.parseDouble(accountInfo.getBalanceAmt());
            accountDto.setBalance(balance);

            // 가장 잔액이 많은 계좌 확인
            if (balance > maxBalance) {
                maxBalance = balance;
                highestBalanceAccountId = accountDto.getAccountId();
            }

            // 거래 내역 추가
            if (accountId != null) {
                TransactionListRequestDto transactionRequest = new TransactionListRequestDto();
                transactionRequest.setAccountId(accountDto.getAccountId());
                transactionRequest.setFromDate(fromDate);
                transactionRequest.setToDate(toDate);
                List<TransactionResponseDto2> transactions = fetchTransactionList(transactionRequest);
                accountDto.setTransactionList(transactions);
            }

            // 계좌 리스트에 추가
            accountList.add(accountDto);
        }
        response.setAccountList(accountList);

        // 메인 계좌 추가 - 최초엔 잔액이 가장 많은 계좌로
        MainAccount mainAccount = mainAccountRepository.findByMemberId(member.getId()).orElse(null);
        if (mainAccount == null) {
            // 메인 계좌 DB INSERT
            Account highestAccount = accountRepository.findById(highestBalanceAccountId).orElse(null);
            if (highestAccount != null) {
                mainAccount = MainAccount.createMainAccount(member, highestAccount);
                mainAccountRepository.save(mainAccount);

                log.info("HomeService::mapToAccountMemberResponseDto 메인계좌 INSERT - 메인계좌 ID: {}", highestAccount.getId());
            }
        }
        response.setMainAccountId(mainAccount.getAccount().getId());


        return response;
    }

    /**
     * 테스트베드 요청: 거래내역 조회
     */
    private List<TransactionResponseDto2> fetchTransactionList(TransactionListRequestDto requestDto) {
        // 현재 시점
        LocalDateTime now = LocalDateTime.now();

        // fromDate: 1년 전 시점
        String fromDate = now.minusYears(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // toDate: 현재 시점
        String toDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // tranDtime: 현재 날짜와 시간
        String tranDtime = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        // API 요청 DTO 생성
        TransactionListApiRequestDto apiRequestDto = new TransactionListApiRequestDto();
        apiRequestDto.setFintechUseNum(accountRepository.findById(requestDto.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("계좌가 존재하지 않습니다."))
                .getFintechUseNum());
        apiRequestDto.setInquiryType("A");
        apiRequestDto.setInquiryBase("D");
        apiRequestDto.setFromDate(fromDate);
        apiRequestDto.setFromTime("001000");
        apiRequestDto.setToDate(toDate);
        apiRequestDto.setToTime("240000");
        apiRequestDto.setSortOrder("D");
        apiRequestDto.setTranDtime(tranDtime); // 동적으로 설정된 트랜잭션 시간 추가
        apiRequestDto.setDataLength("5"); // 기본값


        TransactionListApiResponseDto responseDto = testbedApiClient.requestApi(apiRequestDto, "/openbank/tranlist", TransactionListApiResponseDto.class);

        if (responseDto.getResList() == null || responseDto.getResList().isEmpty()) {
            return List.of();
        }

        return responseDto.getResList().stream()
                .map(transaction -> {
                    TransactionResponseDto2 transactionDto = new TransactionResponseDto2();
                    transactionDto.setTranNum(transaction.getTranNum());
                    transactionDto.setInoutType(transaction.getInoutType());
                    transactionDto.setPrintContent(transaction.getPrintContent());

                    // 출금(송금 포함)의 경우 금액을 음수로 변경
                    if ("출금".equals(transaction.getInoutType())) {
                        transactionDto.setTranAmt("-" + transaction.getTranAmt());
                    } else {
                        transactionDto.setTranAmt(transaction.getTranAmt());
                    }

                    transactionDto.setTranDateTime(transaction.getTranDate() + " " + transaction.getTranTime());
                    return transactionDto;
                })
                .collect(Collectors.toList());
        }
}
