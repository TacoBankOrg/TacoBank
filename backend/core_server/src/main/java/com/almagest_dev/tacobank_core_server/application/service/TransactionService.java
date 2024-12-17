package com.almagest_dev.tacobank_core_server.application.service;

import com.almagest_dev.tacobank_core_server.domain.account.model.Account;
import com.almagest_dev.tacobank_core_server.domain.account.repository.AccountRepository;
import com.almagest_dev.tacobank_core_server.domain.bankCode.OrgCode;
import com.almagest_dev.tacobank_core_server.domain.bankCode.OrgCodeRepository;
import com.almagest_dev.tacobank_core_server.infrastructure.external.testbed.client.TestbedApiClient;
import com.almagest_dev.tacobank_core_server.infrastructure.external.testbed.dto.TransactionDetailApiDto;
import com.almagest_dev.tacobank_core_server.infrastructure.external.testbed.dto.TransactionListApiRequestDto;
import com.almagest_dev.tacobank_core_server.infrastructure.external.testbed.dto.TransactionListApiResponseDto;
import com.almagest_dev.tacobank_core_server.presentation.dto.transantion.TransactionListRequestDto;
import com.almagest_dev.tacobank_core_server.presentation.dto.transantion.TransactionDetails;
import com.almagest_dev.tacobank_core_server.presentation.dto.transantion.TransactionResponseDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TestbedApiClient testbedApiClient;
    private final AccountRepository accountRepository;
    private final OrgCodeRepository orgCodeRepository;

    @Transactional
    public TransactionResponseDto getTransactionList(TransactionListRequestDto requestDto) {
        // 계좌 조회
        Account account = accountRepository.findByIdAndVerified(requestDto.getAccountId(), "Y")
                .orElseThrow(() -> new IllegalArgumentException("본인 인증된 해당 계좌가 존재하지 않습니다."));

        // @TODO 해당 부분 필요한지 확인
        //if (accounts.size() > 1) {
        //    throw new IllegalArgumentException("해당 계좌에 대해 중복된 데이터가 존재합니다. 관리자에게 문의하세요.");
        //}
        // 유일한 계좌를 가져옴
        // Account account = accounts.get(0);

        // 계좌 금융 ID 조회
        String fintechUseNum = account.getFintechUseNum();
        if (fintechUseNum == null || fintechUseNum.isEmpty()) {
            throw new IllegalArgumentException("계좌 정보가 유효하지 않습니다. 관리자에게 문의해주세요.");
        }

        // 거래내역 API 호출 요청 생성 및 데이터 조회
        TransactionListApiResponseDto responseDto = fetchTransactionListFromApi(fintechUseNum, requestDto);
        List<TransactionDetails> transactionDetails =  mapToTransactionResponseDto(responseDto.getResList());

        // 응답 매핑
        OrgCode orgCode = orgCodeRepository.findByCode(account.getBankCode()).orElse(null); // 은행 코드 조회

        return new TransactionResponseDto(
                account.getId(),
                account.getAccountName(),
                account.getAccountNum(),
                account.getAccountHolderName(),
                orgCode.getName(),
                account.getBankCode(),
                responseDto.getBalanceAmt(),
                transactionDetails
        );
    }

    private TransactionListApiResponseDto fetchTransactionListFromApi(String fintechUseNum, TransactionListRequestDto requestDto) {

        LocalDateTime now = LocalDateTime.now();
        String tranDtime = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        // API 요청 DTO 생성
        TransactionListApiRequestDto apiRequestDto = new TransactionListApiRequestDto();
        apiRequestDto.setFintechUseNum(fintechUseNum); // 금융 ID
        apiRequestDto.setInquiryType("A"); // 기본값: 전체 조회
        apiRequestDto.setInquiryBase("D"); // 기본값: 날짜 기준
        apiRequestDto.setFromDate(requestDto.getFromDate()); // 조회 시작 날짜
        apiRequestDto.setFromTime("001000"); // 기본값
        apiRequestDto.setToDate(requestDto.getToDate()); // 조회 종료 날짜
        apiRequestDto.setToTime("240000"); // 기본값
        apiRequestDto.setSortOrder("D"); // 기본값: 내림차순
        apiRequestDto.setTranDtime(tranDtime); // 디폴트 값
        apiRequestDto.setDataLength(""); // 기본값

        // API 호출
        TransactionListApiResponseDto responseDto = testbedApiClient.requestApi(
                apiRequestDto,
                "/openbank/tranlist",
                TransactionListApiResponseDto.class
        );

        // 거래 내역 확인
        if (responseDto.getResList() == null || responseDto.getResList().isEmpty()) {
            System.out.println("API 응답의 거래 내역이 비어 있습니다.");
        } else {
            System.out.println("API 응답의 거래 내역: " + responseDto.getResList());
        }

        return responseDto;
    }

    private List<TransactionDetails> mapToTransactionResponseDto(List<TransactionDetailApiDto> transactions) {

        if (transactions == null || transactions.isEmpty()) {
            return List.of(); // 빈 리스트 반환
        }

        // API 응답 데이터를 클라이언트로 반환하기 위한 DTO로 매핑
        return transactions.stream()
                .map(transaction -> {
                    TransactionDetails responseDto = new TransactionDetails();
                    responseDto.setTranNum(transaction.getTranNum()); // 거래 ID가 없는 경우 null 처리
                    responseDto.setType(transaction.getTranType());
                    responseDto.setAfterBalanceAmount(Double.valueOf(transaction.getAfterBalanceAmt()));
                    responseDto.setTranDateTime(transaction.getTranDate() + " " + transaction.getTranTime());
                    String tranAmt = "출금".equals(transaction.getInoutType()) ? "-" + transaction.getTranAmt() : transaction.getTranAmt();
                    responseDto.setTranAmt(tranAmt);
                    responseDto.setInoutType(transaction.getInoutType());
                    responseDto.setPrintContent(transaction.getPrintContent());
                    return responseDto;
                })
                .collect(Collectors.toList());
    }
}