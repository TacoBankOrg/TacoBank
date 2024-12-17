import React, { useEffect, useRef, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { TransferReq } from "../../types/transferTypes";
import TransferPinModal from "../../components/TransferPinModal";
import { sendMoney } from "../../api/transferApi";
import axios from "axios";

interface LocationState {
  idempotencyKey: string;
  memberId: number;
  withdrawalDetails: {
    accountId: number;
    accountNum: string;
    accountHolder: string;
    bankCode: string;
  };
  receiverDetails: {
    bankCode: string;
    accountNum: string;
    accountHolder: string;
  };
  amount: number;
}

const ConfirmTransfer: React.FC = () => {
  const navigate = useNavigate();
  const [isPinModalOpen, setIsPinModalOpen] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | undefined>(
    undefined
  );
  const location = useLocation(); // useLocation 훅 호출
  const state = location.state as LocationState;
  const {
    idempotencyKey,
    memberId,
    withdrawalDetails,
    receiverDetails,
    amount,
  } = state || {};

  const [rcvPrintContent, setRcvPrintContent] = useState<string>(
    state?.receiverDetails?.accountHolder || ""
  );
  const [wdPrintContent, setWdPrintContent] = useState<string>(
    state?.withdrawalDetails?.accountHolder || ""
  );

  const rcvInputRef = useRef<HTMLInputElement>(null);
  const wdInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (!state) {
      // state가 없으면 이전 페이지로 리다이렉트
      navigate("/transfer", { replace: true });
    }
  }, [state, navigate]);

  // state가 없으면 컴포넌트 렌더링하지 않음
  if (!state) {
    return null;
  }
  const handlePinEntered = async (enteredPin: string): Promise<boolean> => {
    try {
      const dto: TransferReq = {
        idempotencyKey,
        memberId,
        settlementId: null,
        withdrawalDetails: {
          accountId: withdrawalDetails.accountId,
          accountNum: withdrawalDetails.accountNum,
          accountHolder: withdrawalDetails.accountHolder,
          bankCode: withdrawalDetails.bankCode,
        },
        receiverDetails: {
          bankCode: receiverDetails.bankCode,
          accountNum: receiverDetails.accountNum,
          accountHolder: receiverDetails.accountHolder,
        },
        amount: Number(amount),
        rcvPrintContent,
        wdPrintContent,
        transferPin: enteredPin,
      };

      const response = await sendMoney(dto);

      if (response.status === "SUCCESS") {
        setIsPinModalOpen(false);
        console.log("PIN 검증 성공:", response);

        // 페이지 이동은 반환 이후에 실행
        setTimeout(() => {
          navigate("/transfer/success", {
            state: {
              amount: response.data.amount,
              accountHolder: receiverDetails.accountHolder,
              accountNum: receiverDetails.accountNum,
              bankCode: response.data.receiverDetails.bankCode,
            },
          });
        }, 0);

        return true; // 성공 시 true 반환
      } else {
        alert(response.message || "오류가 발생했습니다.");
        return false;
      }
    } catch (error) {
      if (axios.isAxiosError(error)) {
        console.log(error);

        const errorData = error.response?.data;
        if (errorData) {
          if (errorData.status === "FAILURE") {
            setErrorMessage(errorData.message || "잘못된 송금 요청입니다.");
            alert(errorData.message || "잘못된 송금 요청입니다.");

          } else if (errorData.status === "TERMINATED") {
            alert(
              errorData.message || "잘못된 송금 요청입니다. 송금을 종료합니다."
            );

            setIsPinModalOpen(false); // PIN 모달 닫기
            navigate("/"); // 홈으로 이동
          }
        }
      }
      return false; // 오류 발생 시 false 반환
    }
  };

  const handleAuthFallback = () => {
    // PIN 실패 5회 시 송금 차단 안내 및 본인 인증 모달 열기
    setIsPinModalOpen(false); // PIN 모달 닫기
    alert(
      "PIN 입력이 5회 실패하여 송금이 차단되었습니다. 본인 인증 후 PIN을 재설정하세요."
    );
    navigate("/");
  };

  const handleConfirm = () => {
    setIsPinModalOpen(true);
  };

  return (
    <div
      id="app-container"
      className="min-h-screen flex flex-col justify-between p-6"
    >
      {/* 헤더 */}
      <header className="relative flex justify-center items-center mb-6">
        <h1 className="text-lg font-medium">송금확인</h1>
        <button
          onClick={() => {
            sessionStorage.setItem("amount", amount.toString()); // 세션에 금액 저장
            navigate(-1); // 뒤로가기
          }}
          className="absolute right-0"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            strokeWidth={2.0}
            stroke="currentColor"
            className="size-6"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              d="M6 18 18 6M6 6l12 12"
            />
          </svg>
        </button>
      </header>

      {/* 본문 */}
      <div className="flex flex-col flex-grow">
        {/* 제목 및 금액 */}
        <div className="text-center  mb-6">
          <p className="text-xl font-bold">
            <span className="text-[#536DFE] font-bold">
              {receiverDetails.accountHolder}
            </span>
            님께 <span className="text-[#536DFE]">{amount}원</span>을
          </p>
          <p className="text-xl font-bold">송금할까요?</p>
        </div>

        {/* 정보 표시 */}
        <div className="space-y-4 mb-8">
          <div className="flex justify-between">
            <span className="text-[#757575] text-m">받는 계좌</span>
            <span className="text-m font-medium">
              {receiverDetails.accountNum}
            </span>
          </div>
          <div className="flex justify-between">
            <span className="text-[#757575] text-m">출금 계좌</span>
            <span className="text-m font-medium">
              {withdrawalDetails.accountNum}
            </span>
          </div>
          <div className="flex justify-between items-center">
            <span className="text-[#757575] text-m">받는 분에게 표시</span>
            <div className="flex items-center space-x-2 border-b">
              <input
                type="text"
                ref={wdInputRef}
                value={wdPrintContent}
                onChange={(e) => setWdPrintContent(e.target.value)}
                className="text-m flex-grow text-right"
              />
              <svg
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                strokeWidth={1.5}
                stroke="currentColor"
                className="w-4 h-4"
                onClick={() => wdInputRef.current?.focus()}
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="m16.862 4.487 1.687-1.688a1.875 1.875 0 1 1 2.652 2.652L10.582 16.07a4.5 4.5 0 0 1-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 0 1 1.13-1.897l8.932-8.931Zm0 0L19.5 7.125M18 14v4.75A2.25 2.25 0 0 1 15.75 21H5.25A2.25 2.25 0 0 1 3 18.75V8.25A2.25 2.25 0 0 1 5.25 6H10"
                />
              </svg>
            </div>
          </div>
          <div className="flex justify-between items-center">
            <span className="text-[#757575] text-m">나에게 표시</span>
            <div className="flex items-center space-x-2 border-b">
              <input
                type="text"
                ref={rcvInputRef}
                value={rcvPrintContent}
                onChange={(e) => setRcvPrintContent(e.target.value)}
                className="text-m flex-grow text-right"
              />
              <svg
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                strokeWidth={1.5}
                stroke="currentColor"
                className="w-4 h-4"
                onClick={() => rcvInputRef.current?.focus()}
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="m16.862 4.487 1.687-1.688a1.875 1.875 0 1 1 2.652 2.652L10.582 16.07a4.5 4.5 0 0 1-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 0 1 1.13-1.897l8.932-8.931Zm0 0L19.5 7.125M18 14v4.75A2.25 2.25 0 0 1 15.75 21H5.25A2.25 2.25 0 0 1 3 18.75V8.25A2.25 2.25 0 0 1 5.25 6H10"
                />
              </svg>
            </div>
          </div>
        </div>
      </div>

      {/* 하단 송금 버튼 */}
      <div className="p-4">
        <button
          onClick={handleConfirm}
          className="w-full bg-[#536DFE] hover:bg-[#485acf] text-white py-2 rounded-lg font-medium"
        >
          송금
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
  );
};

export default ConfirmTransfer;