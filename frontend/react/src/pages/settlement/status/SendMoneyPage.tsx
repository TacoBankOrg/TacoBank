import { useQueryClient } from "@tanstack/react-query";
import axios from "axios";
import React, { useEffect, useState } from "react";
import Select, { SingleValue } from "react-select";
import { IncludedSettlement } from "../../../types/settlementTypes";
import useAuth from "../../../hooks/useAuth.ts";
import { v4 as uuidv4 } from "uuid";
import {
  baroTransfer,
  checkReceiver,
  sendMoney,
} from "../../../api/transferApi.ts";
import {
  Account,
  BaroTransferReq,
  CheckReceiverReq,
  TransferReq,
} from "../../../types/transferTypes.ts";
import { bankData } from "../../../data/bankData.ts";
import TransferPinModal from "../../../components/TransferPinModal.tsx";
import { useNavigate, useOutletContext } from "react-router-dom";
import { handleApiError } from "../../../utils/apiHelpers.ts";

// 송금 API 호출
const SendMoney: React.FC = () => {
  // React Query를 통해 사용자 정보 가져오기
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const { member } = useAuth();
  const [showSlide, setShowSlide] = useState(false);
  const [selectedAccount, setSelectedAccount] = useState<string>(
    member?.mainAccountId?.toString() || "" // 기본 값으로 메인 계좌 설정
  );
  const [accountBalances, setAccountBalances] = useState<Account[]>([]);
  const [idempotencyKey, setIdempotencyKey] = useState<string>("");
  const [isPinModalOpen, setIsPinModalOpen] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | undefined>(
    undefined
  );
  const [selectedSettlement, setSelectedSettlement] =
    useState<null | IncludedSettlement>(null);
  const memberId = member?.memberId;
  const memberName = member?.name;
  const [menuIsOpen, setMenuIsOpen] = useState(false);

  // 정산 데이터 가져오기
  const { includedSettlements } = useOutletContext<{
    includedSettlements: IncludedSettlement[];
  }>();

  // React-Select 옵션 타입 정의
  interface AccountOption {
    value: string;
    label: string;
    isDisabled: boolean;
  }

  // 계좌 옵션 생성
  const options: AccountOption[] =
    accountBalances?.map((account) => ({
      value: account.accountId!.toString(),
      label: `${account.bankName || "은행이름없음"}(${
        account.accountNum?.slice(-4) || "없음"
      }) : ${account.balance!.toLocaleString()}원`,
      isDisabled: account.balance! < (selectedSettlement?.memberAmount || 0),
    })) || [];

  const customStyles = {
    control: (base: any) => ({
      ...base,
      width: "100%",
      height: "42px",
      border: menuIsOpen ? "1px solid #536DFE" : "1px solid #ccc",
      boxShadow: "none",
      borderRadius: "6px",
      "&:hover": {
        borderColor: menuIsOpen ? "#536DFE" : "#ccc",
      },
    }),
    valueContainer: (base: any) => ({
      ...base,
      padding: "0 12px",
      height: "100%",
      display: "flex",
      alignItems: "center",
    }),
    menu: (base: any) => ({
      ...base,
      position: "absolute",
      marginTop: 0,
      boxShadow: "none",
      overflowY: "auto",
      backgroundColor: "white",
      zIndex: 999,
      border: "1px solid #ccc",
      borderRadius: "6px",
    }),
    option: (base: any, { isDisabled }: { isDisabled: boolean }) => ({
      ...base,
      backgroundColor: isDisabled ? "#f5f5f5" : "white",
      color: isDisabled ? "#aaa" : "#262626",
      cursor: isDisabled ? "not-allowed" : "pointer",
      "&:hover": {
        backgroundColor: isDisabled ? "#f5f5f5" : "#f0f0f0",
      },
    }),
    placeholder: (base: any) => ({
      ...base,
      color: "#aaa",
      margin: "0",
      padding: "0",
    }),
    singleValue: (base: any) => ({
      ...base,
      color: "#262626",
      margin: "0",
      padding: "0",
    }),
    dropdownIndicator: (base: any, state: any) => ({
      ...base,
      transform: state.selectProps.menuIsOpen
        ? "rotate(180deg)"
        : "rotate(0deg)",
    }),
    indicatorSeparator: () => ({
      display: "none",
    }),
  };

  useEffect(() => {
    // 송금 금액보다 잔액이 많은 계좌 중에서 가장 잔액이 많은 계좌를 자동 선택
    if (selectedSettlement) {
      const suitableAccount = accountBalances
        .filter((account) => account.balance! >= selectedSettlement.memberAmount) // 송금 금액보다 잔액이 많은 계좌 필터링
        .sort((a, b) => b.balance! - a.balance!)[0]; // 잔액이 많은 순으로 정렬 후 첫 번째 계좌 선택
      if (suitableAccount) {
        setSelectedAccount(suitableAccount.accountId!.toString());
      } else {
        setSelectedAccount(""); // 잔액 충분한 계좌가 없을 경우 초기화
      }
    }
  }, [selectedSettlement, accountBalances]);

  // 계좌 변경 핸들러
  const handleAccountChange = (selectedOption: SingleValue<AccountOption>) => {
    setSelectedAccount(selectedOption?.value || "");
  };

  const toggleSlide = () => {
    setShowSlide(!showSlide); // 슬라이드 상태 토글
  };

  const getFormattedDate = (date: string) => {
    const parsedDate = new Date(date);
    const year = String(parsedDate.getFullYear()).slice(2);
    const month = parsedDate.getMonth() + 1;
    const day = parsedDate.getDate();
    return `${year}년 ${month}월 ${day}일`;
  };
  const handleSendClick = async (settlement: IncludedSettlement) => {
    if (!settlement) {
      alert("유효하지 않은 정산 항목입니다.");
      return;
    }

    // 정산 상태가 완료된 경우 처리하지 않음
    if (settlement.memberStatus === "Y") {
      alert("정산이 이미 완료된 항목입니다.");
      return;
    }

    // 설정
    setSelectedSettlement(settlement);
    const idempotencyKey = uuidv4();
    const dto: BaroTransferReq = {
      idempotencyKey: idempotencyKey,
      memberId: memberId as number,
      settlementId: settlement.settlementId,
      amount: settlement.memberAmount,
    };
    console.log(settlement.settlementId);
    console.log(dto);
    try {
      // 사용가능 계좌 조회 API 호출
      const response = await baroTransfer(dto);
      console.log(response);
      setAccountBalances(response.accountInfoWithBalances);
      setIdempotencyKey(response.idempotencyKey);
      // 슬라이드 표시 및 선택된 정산 항목 설정
      setSelectedSettlement(settlement);
      setShowSlide(true);
    } catch (error) {
      if (axios.isAxiosError(error)) {
        const errorMessage = handleApiError(error); // 에러 메시지 처리
        alert(errorMessage);
      }
    }
  };
  const getBankCode = (bankName: string): string => {
    const bank = bankData.find((bank) => bank.bankName === bankName);
    return bank ? bank.bankCode : "알 수 없는 은행";
  };
  const selectedAccountId = Number(selectedAccount);
  const handleSendMoney = async () => {
    if (!selectedAccount || !selectedSettlement) {
      alert("계좌를 선택해주세요.");
      return;
    }
    const receiverBankCode = getBankCode(
      selectedSettlement.account.bankName || ""
    );

    // DTO 정의
    const dto: CheckReceiverReq = {
      idempotencyKey,
      withdrawalMemberId: memberId as number,
      settlementId: selectedSettlement.settlementId,
      withdrawalAccountId: selectedAccountId,
      receiverBankCode: receiverBankCode,
      receiverAccountNum: selectedSettlement.account.accountNum,
    };
    console.log(dto);

    try {
      // 수취인 조회 API 호출
      const response = await checkReceiver(dto);

      console.log(response);
      setIsPinModalOpen(true); // PIN 모달 표시
    } catch (error) {
      if (axios.isAxiosError(error)) {
        const errorMessage = handleApiError(error); // 에러 메시지 처리
        alert(errorMessage);
      }
    }
  };

  const handlePinEntered = async (enteredPin: string): Promise<boolean> => {
    const selectedAccountId = Number(selectedAccount); // 선택된 계좌 ID
    const selectedAccountInfo = accountBalances.find(
      (account) => account.accountId === selectedAccountId
    );
    if (!selectedAccountInfo || !selectedSettlement) {
      alert("선택한 계좌 정보 또는 정산 정보가 없습니다.");
      return false;
    }
    const selectedAccountNum = selectedAccountInfo.accountNum;
    const selectedBankCode = getBankCode(selectedAccountInfo.bankName || "");
    const receiverBankCode = getBankCode(
      selectedSettlement.account.bankName || ""
    );

    try {
      const dto: TransferReq = {
        idempotencyKey,
        memberId: memberId as number,
        settlementId: selectedSettlement.settlementId,
        withdrawalDetails: {
          accountId: selectedAccountId,
          accountNum: selectedAccountNum,
          accountHolder: memberName as string,
          bankCode: selectedBankCode,
        },
        receiverDetails: {
          bankCode: receiverBankCode,
          accountNum: selectedSettlement.account.accountNum,
          accountHolder: selectedSettlement.account.accountHolder,
        },
        amount: selectedSettlement.memberAmount,
        rcvPrintContent: selectedSettlement.account.accountHolder,
        wdPrintContent: memberName as string,
        transferPin: enteredPin,
      };

      const response = await sendMoney(dto);

      if (response.status === "SUCCESS") {
        setIsPinModalOpen(false);
        console.log("PIN 검증 성공:", response);

        // 캐시 무효화로 정산 상태 갱신
        queryClient.invalidateQueries({ queryKey: ["settlements", memberId] });

        // 성공 후 페이지 이동 (예: 성공 페이지로)
        setTimeout(() => {
          navigate("/transfer/success", {
            state: {
              amount: selectedSettlement.memberAmount,
              accountHolder: selectedSettlement.account.accountHolder,
              accountNum: selectedSettlement.account.accountNum,
              bankCode: receiverBankCode,
            },
          });
        }, 0);
        return true; // 성공 시 true 반환
      } else if (response.status === "FAILURE") {
        console.error("송금 실패:", response);
        setErrorMessage(response.message || "PIN 번호가 올바르지 않습니다.");
        return false;
      } else {
        console.error("송금 취소 또는 중단:", response);
        setIsPinModalOpen(false); // PIN 모달 닫기
        navigate("/"); // 홈으로 이동
        return false; // 기본 처리로 터미네이트 포함
      }
    } catch (error) {
      if (axios.isAxiosError(error)) {
        setErrorMessage(handleApiError(error)); // 에러 메시지 설정
      }
      return false; // 오류 발생 시 false 반환
    }
  };
  const handleAuthFallback = () => {
    // PIN 실패 5회 시 송금 차단 안내 및 본인 인증 모달 열기
    setIsPinModalOpen(false); // PIN 모달 닫기
    navigate("/")
  };

  return (
    <div
    className="flex flex-col gap-4 overflow-y-auto" style={{ maxHeight: "calc(100vh - 72px)", }}>
        {includedSettlements.length === 0 ? (
          <p className="text-center text-[#757575]">표시할 정산 데이터가 없습니다.</p>
        ) : (
          includedSettlements.map((settlement: IncludedSettlement) => (
            <div key={settlement.settlementId} className="mb-4">
              {settlement.memberStatus === "Y" && settlement.leaderId !== memberId && (
                <div className="text-sm text-gray-500 mb-2">
                  {getFormattedDate(settlement.createdDate)}
                </div>
              )}
              {settlement.leaderId !== memberId && (
                <div
                  className={`bg-[#EAEDFF] p-3 rounded-lg flex justify-between items-center ${
                    settlement.memberStatus === "N" ? "" : "opacity-50"
                  }`}
                >
                  <div>
                    <div className="font-bold">
                      {settlement.account.accountHolder}
                    </div>
                    <div className="font-bold text-lg">
                      {settlement.memberAmount.toLocaleString()}원
                    </div>
                  </div>
                  {settlement.memberStatus === "N" ? (
                    <button
                      onClick={() => handleSendClick(settlement)}
                      className="px-3 py-2 bg-[#536DFE] text-white rounded-lg text-sm hover:bg-[#485acf]"
                    >
                      송금
                    </button>
                  ) : (
                    <div className="text-gray-500 text-sm">완료</div>
                  )}
                </div>
              )}
            </div>
          ))
        )}
     
  
      {/* 슬라이드 메뉴 */}
      {showSlide && selectedSettlement && (
        <div
        className="absolute bottom-0 left-0 bg-white border-x border-t border-gray-300 pb-4 px-4 z-50 rounded-t-3xl flex flex-col"
        style={{
          width: "100%",
          height: "50vh",
        }}
      >
          <div className="flex justify-center items-center">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
              strokeWidth={2.5}
              stroke="#e5e7eb"
              className="size-6"
              onClick={toggleSlide}
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="m19.5 8.25-7.5 7.5-7.5-7.5"
              />
            </svg>
          </div>
          <div className="mt-4">
            <Select
              options={options}
              value={options.find(
                (option) => option.value.toString() === selectedAccount // 기본 값 설정
              )}
              onChange={handleAccountChange}
              isSearchable={false}
              styles={customStyles}
              blurInputOnSelect={true}
              menuIsOpen={menuIsOpen}
              onMenuOpen={() => setMenuIsOpen(true)}
              onMenuClose={() => setMenuIsOpen(false)}
            />
          </div>
          <p className="text-center text-xl font-bold mt-4 mb-8">
            {selectedSettlement.account.accountHolder}님께
            <br /> {selectedSettlement.memberAmount.toLocaleString()}원을 송금할까요?
          </p>
          <div className="text-sm mt-4">
            <p>예금주: {selectedSettlement.account.accountHolder}</p>
            <p>
              {selectedSettlement.account.bankName} {selectedSettlement.account.accountNum}
            </p>
            <p>송금 금액: {selectedSettlement.memberAmount.toLocaleString()}원</p>
          </div>
          <div className="mt-auto">
            <p className="text-red-500 text-xl text-center font-bold">
              송금이 바로 됩니다
            </p>
            <button
              onClick={handleSendMoney}
              className="w-full py-2 bg-[#536DFE] text-white rounded-lg text-sm hover:bg-[#485acf]"
            >
              바로 송금
            </button>
          </div>
          <TransferPinModal // 출금 비번용
            isOpen={isPinModalOpen}
            onClose={() => setIsPinModalOpen(false)}
            onPinEntered={handlePinEntered}
            onAuthFallback={handleAuthFallback}
            errorMessage={errorMessage}
          />
        </div>
      )}
  
   
    </div>
  );
};

export default SendMoney;
