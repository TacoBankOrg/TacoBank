package com.almagest_dev.tacobank_core_server.application.service;

import com.almagest_dev.tacobank_core_server.domain.account.model.Account;
import com.almagest_dev.tacobank_core_server.domain.account.repository.AccountRepository;
import com.almagest_dev.tacobank_core_server.domain.account.repository.MainAccountRepository;
import com.almagest_dev.tacobank_core_server.domain.group.model.Group;
import com.almagest_dev.tacobank_core_server.domain.group.model.GroupMember;
import com.almagest_dev.tacobank_core_server.domain.group.repository.GroupMemberRepository;
import com.almagest_dev.tacobank_core_server.domain.group.repository.GroupRepository;
import com.almagest_dev.tacobank_core_server.domain.member.model.Member;
import com.almagest_dev.tacobank_core_server.domain.member.repository.MemberRepository;
import com.almagest_dev.tacobank_core_server.domain.receipt.model.*;
import com.almagest_dev.tacobank_core_server.domain.receipt.repository.*;
import com.almagest_dev.tacobank_core_server.domain.settlememt.model.Settlement;
import com.almagest_dev.tacobank_core_server.domain.settlememt.model.SettlementDetails;
import com.almagest_dev.tacobank_core_server.domain.settlememt.repository.SettlementDetailsRepository;
import com.almagest_dev.tacobank_core_server.domain.settlememt.repository.SettlementRepository;
import com.almagest_dev.tacobank_core_server.infrastructure.external.testbed.client.TestbedApiClient;

import com.almagest_dev.tacobank_core_server.presentation.dto.account.AccountInfoWithBalance;
import com.almagest_dev.tacobank_core_server.presentation.dto.account.AccountDto;
import com.almagest_dev.tacobank_core_server.presentation.dto.receipt.ProductMemberDetails;
import com.almagest_dev.tacobank_core_server.presentation.dto.settlement.*;
import com.almagest_dev.tacobank_core_server.infrastructure.external.testbed.dto.BalanceInquiryApiRequestDto;
import com.almagest_dev.tacobank_core_server.infrastructure.external.testbed.dto.BalanceInquiryApiResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final SettlementRepository settlementRepository;
    private final SettlementDetailsRepository settlementDetailsRepository;
    private final MainAccountRepository mainAccountRepository;
    private final NotificationService notificationService;
    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;
    private final ReceiptRepository receiptRepository;
    private final ReceiptProductRepository receiptProductRepository;
    private final ReceiptMemberRepository receiptMemberRepository;
    private final TestbedApiClient testbedApiClient;
    private final OrgCodeService orgCodeService;

    /**
     * 정산 요청
     *  - 그룹 생성 여부 확인 / 커스텀 N이면 그룹 생성
     *  - 영수증 정산 분기 처리
     */
    @Transactional
    public void processSettlementRequest(SettlementRequestDto request) {
        // 영수증 정산인 경우 Null Check
        boolean receiptFlag = false;
        Long receiptId = null;
        if (request.getType().equals("receipt")) {
            receiptFlag = true;
            if (request.getReceiptId() == null) {
                throw new IllegalArgumentException("영수증 정보를 보내주세요.");
            }
            if (request.getProductMemberDetails() == null) {
                throw new IllegalArgumentException("영수증 품목별 멤버 정보를 보내주세요.");
            }
            receiptId = request.getReceiptId();
        }

        Group group;
        // 1. 그룹 확인 또는 생성
        if (request.getGroupId() == null) {
            // 친구 선택으로 그룹 생성
            group = createTemporaryGroup(request);
        } else {
            // 기존 그룹 조회
            group = groupRepository.findByIdAndLeaderId(request.getGroupId(), request.getLeaderId())
                    .orElseThrow(() -> new IllegalArgumentException("그룹이 존재하지 않거나 요청한 사용자가 그룹 리더가 아닙니다."));
        }

        // 선택된 계좌 확인
        Account selectedAccount = accountRepository.findById(request.getSettlementAccountId())
                .orElseThrow(() -> new IllegalArgumentException("선택된 계좌가 존재하지 않습니다."));

        // 리더 ID와 계좌 소유자 검증
        if (!selectedAccount.getMember().getId().equals(request.getLeaderId())) {
            throw new IllegalArgumentException("선택된 계좌는 정산 요청을 한 리더의 계좌가 아닙니다.");
        }


        // 2. 총 금액과 개별 정산 금액 검증
        long calculatedTotalAmount = request.getMemberAmounts().stream()
                .mapToLong(SettlementMemberDto::getAmount)
                .sum();

        if (calculatedTotalAmount != request.getTotalAmount() || calculatedTotalAmount == 0) {
            throw new IllegalArgumentException(String.format(
                    "총 정산 금액(%d)과 개별 금액 합계(%d)가 일치하지 않거나 총액이 0입니다.",
                    request.getTotalAmount(), calculatedTotalAmount
            ));
        }


        // 3. 정산 데이터 생성 및 저장
        Settlement settlement = new Settlement();
        settlement.savePayGroup(group);
        settlement.saveReceiptId(receiptId);
        settlement.saveSettlementAccount(selectedAccount);
        settlement.saveSettlementTotalAmount(request.getTotalAmount());
        settlement.saveSettlementStatus("N");
        settlementRepository.save(settlement);

        // 4. 개인 멤버별 정산 데이터 저장 및 알림 생성
        //  - 영수증 정산 DB 연산시 사용할 MemberId - GroupMemberId 리스트 생성
        Map<Long, GroupMember> memberMappingGroupMembers = new HashMap<>(); // key: memberId | value: 해당 GroupMember
        for (SettlementMemberDto memberDto : request.getMemberAmounts()) {
            GroupMember groupMember = groupMemberRepository.findByPayGroupAndMemberId(group, memberDto.getMemberId())
                    .orElseThrow(() -> new IllegalArgumentException("그룹에 해당 멤버가 존재하지 않습니다."));

            SettlementDetails settlementDetails = new SettlementDetails();
            settlementDetails.saveSettlement(settlement);
            settlementDetails.saveGroupMember(groupMember);
            settlementDetails.saveSettlementAmount(memberDto.getAmount());

            if (groupMember.getMember().getId().equals(request.getLeaderId())) {
                settlementDetails.saveSettlementStatus("Y");
            } else {
                settlementDetails.saveSettlementStatus("N");
            }

            settlementDetailsRepository.save(settlementDetails);

            // 알림 전송 (비즈니스 로직에서 제거)
            if (!groupMember.getMember().getId().equals(request.getLeaderId())) {
                notificationService.sendNotification(groupMember.getMember(),
                        String.format("정산 요청이 도착했습니다. 요청 금액: %d원", memberDto.getAmount()));
            }

            // 영수증 정산인 경우, MemberId - GroupMemberId 매핑
            if (receiptFlag) {
                memberMappingGroupMembers.put(memberDto.getMemberId(), groupMember);
                log.info("memberMappingGroupMembers: key - {} , value - {}", memberDto.getMemberId(), groupMember.getId());
            }
        }

        // 영수증 정산인 경우
        if (receiptFlag) {
            processReceiptSettlement(request, memberMappingGroupMembers);
        }
    }

    /**
     * 친구 선택시, 임시 그룹 만들기
     */
    @Transactional
    public Group createTemporaryGroup(SettlementRequestDto request) {
        log.info("createTemporaryGroup START");
        // 그룹장 찾기
        Member leader = memberRepository.findByIdAndDeleted(request.getLeaderId(), "N")
                .orElseThrow(() -> new IllegalArgumentException("요청 그룹장은 존재하지 않는 회원입니다."));

        // 그룹 생성
        Group group = new Group();
        group.createLeader(leader);
        group.createName(leader.getName() + "과 아이들");
        group.createActivated("N");
        group.createCustomized("N");
        groupRepository.save(group);

        log.info("createTemporaryGroup friends : {}", request.getFriendIds());

        // 친구 목록 추가
        for (Long friendId : request.getFriendIds()) {

            Member friend = memberRepository.findByIdAndDeleted(friendId, "N")
                    .orElseThrow(() -> new IllegalArgumentException("친구를 찾을 수 없습니다."));

            GroupMember groupMember = new GroupMember();
            groupMember.savePayGroup(group);
            groupMember.saveMember(friend);
            groupMember.saveStatus("ACCEPTED");
            groupMemberRepository.save(groupMember);
        }
        log.info("createTemporaryGroup END");
        return group;
    }

    /**
     * 영수증 정산 요청
     */
    private void processReceiptSettlement(SettlementRequestDto requestDto, Map<Long, GroupMember> memberMappingGroupMembers) {
        log.info("SettlementService::processReceiptSettlement START");
        // Receipt 조회
        Receipt receipt = receiptRepository.findById(requestDto.getReceiptId()).orElse(null);
        if (receipt == null) {
            log.warn("SettlementService::processReceiptSettlement 조회되는 Receipt가 없습니다. - receiptId: {}", requestDto.getReceiptId());
            return;
        }
        log.info("SettlementService::processReceiptSettlement 요청 정보 - receiptId: {} | productMemberDetails: {}", requestDto.getReceiptId(), requestDto.getProductMemberDetails());

        // ReceiptMember 값 세팅 & ReceiptProduct 조회
        if (requestDto.getProductMemberDetails() == null) {
            log.warn("SettlementService::processReceiptSettlement 영수증 품목별 포함 멤버 정보가 없습니다. - receiptId: {}", requestDto.getReceiptId());
            return;
        }
        // 데이터 세팅
        List<ReceiptMember> receiptMembers = new ArrayList<>(); // 영수증 품목 지불대상자 - 저장할 리스트
        for (ProductMemberDetails productMemberDetail : requestDto.getProductMemberDetails()) {
            if (productMemberDetail.getProductId() < 0) {
                log.warn("SettlementService::processReceiptSettlement 영수증 ID가 없습니다.");
                continue;
            }
            // 영수증 품목 정보 조회
            ReceiptProduct receiptProduct = receiptProductRepository.findById(productMemberDetail.getProductId()).orElse(null);
            if (receiptProduct == null) {
                log.warn("SettlementService::processReceiptSettlement 영수증 품목 정보가 없습니다. - receiptId: {} | productId: {}", requestDto.getReceiptId(), productMemberDetail.getProductId());
                continue;
            }

            // 멤버 ID를 정산 그룹 구성원 ID로 매핑해서 리스트에 저장
            if (productMemberDetail.getProductMembers() == null) {
                log.warn("SettlementService::processReceiptSettlement 영수증 품목별 멤버 리스트가 없습니다. - receiptId: {} | productId: {}", requestDto.getReceiptId(), productMemberDetail.getProductId());
                continue;
            }
            for (Long memberId : productMemberDetail.getProductMembers()) {
                receiptMembers.add(ReceiptMember.createReceiptMember(receiptProduct, memberMappingGroupMembers.get(memberId)));
            }
        }

        // ReceiptMember INSERT
        receiptMemberRepository.saveAll(receiptMembers);
        log.info("SettlementService::processReceiptSettlement END");
    }


    /**
     * 정산 현황 조회하기
     */
    public SettlementStatusResponseDto getSettlementStatus(Long memberId) {
        // 내가 만든 정산 리스트
        List<MyCreatedSettlementDto> createdSettlements = settlementRepository.findByPayGroup_Leader_Id(memberId).stream()
                .map(settlement -> new MyCreatedSettlementDto(
                        settlement.getId(),
                        settlement.getPayGroup().getId(),
                        settlement.getPayGroup().getName(),
                        settlement.getSettlementTotalAmount().longValue(),
                        settlement.getSettlementStatus(),
                        settlement.getCreatedDate(),
                        settlement.getUpdatedDate()
                ))
                .collect(Collectors.toList());

        // 내가 포함된 정산 리스트
        List<MyIncludedSettlementDto> includedSettlements = settlementDetailsRepository.findByGroupMember_Member_Id(memberId).stream()
                .map(detail -> {
                    // 은행 이름 조회
                    String bankName = orgCodeService.getBankNameByCode(detail.getSettlement().getSettlementAccount().getBankCode());

                    return new MyIncludedSettlementDto(
                            detail.getSettlement().getId(),
                            detail.getSettlement().getCreatedDate(),
                            detail.getSettlementAmount().longValue(),
                            detail.getSettlementStatus(),
                            new AccountDto(
                                    detail.getSettlement().getSettlementAccount().getAccountHolderName(),
                                    detail.getSettlement().getSettlementAccount().getAccountNum(),
                                    bankName
                            ),
                            detail.getSettlement().getPayGroup().getLeader().getId()
                    );
                })
                .collect(Collectors.toList());

        // 송금 가능한 계좌 리스트
        List<AccountDto> availableAccounts = accountRepository.findByMember_Id(memberId).stream()
                .map(account -> {
                    // 은행 이름 추가
                    String bankName = orgCodeService.getBankNameByCode(account.getBankCode());
                    return new AccountDto(
                            account.getAccountHolderName(),
                            account.getAccountNum(),
                            bankName
                    );
                })
                .collect(Collectors.toList());

        return new SettlementStatusResponseDto(createdSettlements, includedSettlements, availableAccounts);
    }

    /**
     * 정산 상세 내역 조회하기
     */
    public SettlementDetailsListResponseDto getSettlementDetails(Long settlementId, Long memberId) {
        // Settlement ID에 해당하는 정산 데이터 가져오기
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("정산 ID에 해당하는 정산을 찾을 수 없습니다."));

        // 정산 세부 항목 가져오기
        List<SettlementDetails> detailsList = settlementDetailsRepository.findBySettlement_Id(settlementId);

        // DTO 변환
        List<SettlementDetailsResponseDto2> settlementDetails = detailsList.stream()
                .map(detail -> new SettlementDetailsResponseDto2(
                        detail.getGroupMember().getMember().getId(),
                        detail.getGroupMember().getMember().getName(),
                        detail.getSettlementAmount().longValue(),
                        detail.getSettlementStatus(),
                        detail.getUpdatedDate(),
                        detail.getSettlement().getPayGroup().getLeader().getId()
                ))
                .collect(Collectors.toList());

        return new SettlementDetailsListResponseDto(settlement.getId(), settlementDetails);
    }

    /**
     * 독촉 알림 보내기
     */
    @Transactional
    public void notifyPendingSettlementForMember(Long settlementId, Long memberId) {
        // 특정 Settlement와 관련된 특정 Member의 정산 정보 조회
        SettlementDetails pendingDetail = settlementDetailsRepository.findBySettlement_IdAndGroupMember_Member_IdAndSettlementStatus(
                settlementId, memberId, "N"
        ).orElseThrow(() -> new IllegalArgumentException("해당 멤버의 정산 정보가 없거나 이미 완료된 정산입니다."));

        // 알림 전송
        String message = String.format("정산 요청이 아직 완료되지 않았습니다. 요청 금액: %d원", pendingDetail.getSettlementAmount());
        notificationService.sendNotification(pendingDetail.getGroupMember().getMember(), message);
    }

    /**
     * 바로 송금 - 정산 데이터 검증 & 사용자 전 계좌 잔액 조회
     */
    public SettlementTransferResponseDto validateSettlementsAndGetAvailableBalances(SettlementTransferRequestDto requestDto) {
        log.info("SettlementService::validateSettlementsAndGetAvailableBalances START");
        // 멤버 정보 확인
        Member member = memberRepository.findByIdAndDeleted(requestDto.getMemberId(), "N")
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 정산 정보 확인 (정산 상태가 N인 경우만 송금 할 수 있음)
        Settlement settlement = settlementRepository.findByIdAndSettlementStatus(requestDto.getSettlementId(), "N")
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 이미 완료된 정산입니다."));

        // 사용자의 그룹 멤버 여부 확인
        GroupMember groupMember = groupMemberRepository.findByPayGroupAndMemberId(settlement.getPayGroup(), requestDto.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("정산 그룹에 포함 되어있지 않습니다."));

        // 개별 정산 상세 확인
        SettlementDetails settlementDetails = settlementDetailsRepository.findBySettlement_IdAndGroupMember_Id(
                        settlement.getId(), groupMember.getId())
                .orElseThrow(() -> new IllegalArgumentException("정산 상세정보가 존재하지 않습니다."));
        // 개별 정산 상태 확인
        if (settlementDetails.getSettlementStatus().equals("Y")) {
            throw new IllegalArgumentException("이미 완료된 정산입니다. 거래 내역을 확인해보세요.");
        }
        // 정산 금액 일치여부 확인
        if (requestDto.getAmount() != settlementDetails.getSettlementAmount()) {
            throw new IllegalArgumentException("정산 금액이 상이합니다. 재시도하거나 관리자에게 문의해주세요.");
        }

        // 사용자의 출금 가능한 전 계좌 잔액 조회
        List<Account> availableAccounts = accountRepository.findByMember_IdAndVerified(requestDto.getMemberId(), "Y");
        if (availableAccounts == null) { // 출금 가능한(본인 인증 완료한) 계좌가 없는 경우
            throw new IllegalArgumentException("출금할 수 있는 계좌가 없습니다. 출금을 위한 본인 인증을 진행해주세요.");
        }

        // TestBed 잔액조회 호출
        log.info("SettlementService::validateSettlementsAndGetAvailableBalances 다건 잔액 조회");
        List<AccountInfoWithBalance> accountInfoWithBalances = new ArrayList<>();
        for (Account account : availableAccounts) {
            BalanceInquiryApiRequestDto apiRequestDto = new BalanceInquiryApiRequestDto(
                    member.getUserFinanceId(),
                    account.getFintechUseNum(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
            );
            // API 호출
            BalanceInquiryApiResponseDto apiResponse = testbedApiClient.requestApi(
                    apiRequestDto,
                    "/openbank/account",
                    BalanceInquiryApiResponseDto.class
            );
            log.info("SettlementService::validateSettlementsAndGetAvailableBalances 잔액 조회 Response: {} ", apiResponse);

            // 개별 계좌 잔액 조회 실패 - 해당 계좌 잔액 0원으로 Return
            if (apiResponse.getApiTranId() == null || !apiResponse.getRspCode().equals("A0000") || apiResponse.getBalanceAmt() == null) {
                log.warn("계좌 잔액 조회에 실패했습니다. - " + apiResponse.getRspMessage());
                accountInfoWithBalances.add(new AccountInfoWithBalance(
                        account.getId()
                        , ""
                        , ""
                        , ""
                        , 0
                        )
                );
                continue;
            }
            // 개별 계좌 잔액 조회 성공 - 계좌별 잔액 객체에 추가
            int balance = (apiResponse.getBalanceAmt() == null) ? 0 : Integer.parseInt(apiResponse.getBalanceAmt());
            String bankName = orgCodeService.getBankNameByCode(account.getBankCode());

            accountInfoWithBalances.add(new AccountInfoWithBalance(
                    account.getId()
                    , account.getAccountNum()
                    , account.getBankCode()
                    , bankName
                    , balance
                    )
            );
        }
        if (accountInfoWithBalances == null || accountInfoWithBalances.size() == 0) {
            throw new IllegalArgumentException("출금 가능한 계좌의 잔액 조회 결과가 없습니다.");
        }

        // 응답 반환
        log.info("SettlementService::validateSettlementsAndGetAvailableBalances END");
        return new SettlementTransferResponseDto(requestDto.getIdempotencyKey(), accountInfoWithBalances);
    }
}