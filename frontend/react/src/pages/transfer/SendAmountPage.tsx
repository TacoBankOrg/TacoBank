import React, { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import backIcon from "../../assets/image/icon/backicon.png";
import useAuth from "../../hooks/useAuth";
import { bankData } from "../../data/bankData";

const SendAmount: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const {
    banksCode,
    accountId,
    bankCode,
    accountNumber,
    accountsHolder,
    accountsNumber,
    accountHolder,
    idempotencyKey,
    balance,
  } = location.state || {}; // 데이터 수신
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const { member } = useAuth();
  const [amount, setAmount] = useState<string>(
    sessionStorage.getItem("amount") || "" // 세션 스토리지에서 초기값 가져오기
  );
  const memberId = member?.memberId;

  useEffect(() => {
    if (!location.state) {
      // 데이터가 없으면 이전 페이지로 리다이렉트
      navigate(-1); // -1은 브라우저 기록을 기반으로 이전 페이지로 이동
    }
  }, [location.state, navigate]);

  useEffect(() => {
    sessionStorage.setItem("amount", amount);
  }, [amount]);

  // location.state가 없으면 화면에 아무것도 렌더링하지 않음
  if (!location.state) return null;
  // 금액에 콤마 추가
  const formatAmount = (value: string | number) => {
    return Number(value).toLocaleString();
  };

  const handleKeyPress = (key: string) => {
    setErrorMessage(null); // 키 입력 시 에러 메시지 초기화
    if (key === "delete") {
      setAmount((prev) => prev.slice(0, -1));
    } else if (key === "clear") {
      setAmount("");
    } else if (!isNaN(Number(key))) {
      const newAmount = Number(amount + key);
      if (newAmount <= balance) {
        setAmount(newAmount.toString());
      } else {
        setErrorMessage("잔고를 초과할 수 없습니다."); // 잔고 초과 메시지 설정
      }
    }
  };

  const handleQuickAmount = (value: number) => {
    setErrorMessage(null);
    const newAmount = Number(amount || "0") + value;
    if (newAmount <= balance) {
      setAmount(newAmount.toString());
    } else {
      setErrorMessage("잔고를 초과할 수 없습니다."); // 잔고 초과 메시지 설정
    }
  };

  const handleNext = () => {
    if (amount && Number(amount) > 0) {
      navigate("/transfer/confirm", {
        state: {
          idempotencyKey,
          memberId,
          withdrawalDetails: {
            accountId,
            accountNum: accountsNumber,
            accountHolder: accountsHolder,
            bankCode: banksCode,
          },
          receiverDetails: {
            bankCode: bankCode,
            accountNum: accountNumber,
            accountHolder: accountHolder,
          },
          amount: Number(amount),
        },
      });
    } else {
      alert("보낼 금액을 입력하세요.");
    }
  };

  const getBankName = (bankCode: string): string => {
    const bank = bankData.find((bank) => bank.bankCode === bankCode);
    return bank ? bank.bankName : "알 수 없는 은행";
  };
  return (
    <div
      id="app-container"
      className="min-h-screen flex flex-col justify-between p-6"
    >
      {/* 헤더 */}
      <header className="relative flex items-center">
        <button
          className="p-1"
          onClick={() => {
            sessionStorage.removeItem("amount"); // 뒤로가기 시 세션에서 금액 삭제
            navigate(-1);
          }}
        >
          <img src={backIcon} alt="Back" className="w-8 h-8" />
        </button>
        <div className=" mx-auto">
          <div className="flex flex-col flex-grow text-center">
            <p className="text-base font-medium text-gray-700">
              {accountHolder}
            </p>
            {bankCode && accountNumber && (
              <p className="text-xs text-gray-500">
                {getBankName(bankCode)} {accountNumber}
              </p>
            )}
          </div>
        </div>
      </header>

      {/* 금액 표시 */}
      <div className="flex flex-col items-center py-6">
        {/* 금액 표시 */}
        <div
          className={`text-center text-4xl font-bold ${
            amount ? "text-[#262626]" : "text-gray-300"
          }`}
        >
          {amount ? `${formatAmount(amount)}원` : "보낼 금액"}
        </div>
        {/* 에러 메시지: 숫자 아래에 표시 */}
        {errorMessage && (
          <p className="text-red-500 text-sm mt-2 text-center">
            {errorMessage}
          </p>
        )}
      </div>

      {/* 하단 섹션 */}
      <div className="w-full">
        {/* 빠른 금액 버튼 */}
        <div className="mb-4">
          <p className="text-gray-700 bg-gray-100 p-2 rounded-lg text-sm mb-3">
            {accountsHolder}의 통장 ({accountsNumber?.slice(-4)}):{" "}
            {formatAmount(balance)}원
          </p>
          <div className="grid grid-cols-4 gap-2">
            <button
              onClick={() => handleQuickAmount(1000)}
              className="border border-gray-300 text-gray-700 text-sm py-2 rounded-lg"
            >
              +1천원
            </button>
            <button
              onClick={() => handleQuickAmount(50000)}
              className="border border-gray-300 text-gray-700 text-sm py-2 rounded-lg"
            >
              +5만원
            </button>
            <button
              onClick={() => handleQuickAmount(100000)}
              className="border border-gray-300 text-gray-700 text-sm py-2 rounded-lg"
            >
              +10만원
            </button>
            <button
              onClick={() => setAmount(balance.toString())} // 전액 설정
              className="border border-gray-300 text-gray-700 text-sm py-2 rounded-lg"
            >
              전액
            </button>
          </div>
        </div>

        {/* 키패드 */}
        <div className="grid grid-cols-3 gap-4 w-full mb-4">
          {[
            "1",
            "2",
            "3",
            "4",
            "5",
            "6",
            "7",
            "8",
            "9",
            "00",
            "0",
            "delete",
          ].map((key, index) => (
            <button
              key={index}
              onClick={() => handleKeyPress(key)}
              className="flex items-center justify-center text-2xl font-medium py-4 rounded-lg"
            >
              {key === "delete" ? (
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                  strokeWidth={1.5}
                  stroke="currentColor"
                  className="w-7 h-7"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    d="M12 9.75L14.25 12m0 0l2.25 2.25M14.25 12l2.25-2.25M14.25 12L12 14.25m-2.58 4.92-6.374-6.375a1.125 1.125 0 010-1.59L9.42 4.83c.21-.211.497-.33.795-.33H19.5a2.25 2.25 0 012.25 2.25v10.5a2.25 2.25 0 01-2.25 2.25h-9.284c-.298 0-.585-.119-.795-.33z"
                  />
                </svg>
              ) : (
                key
              )}
            </button>
          ))}
        </div>

        {/* 다음 버튼 */}
        <button
          onClick={handleNext}
          className={`w-full py-2 rounded-lg font-medium ${
            amount
              ? "bg-[#536DFE] text-white hover:bg-[#485acf]"
              : "bg-gray-300 text-gray-500 cursor-not-allowed"
          }`}
          disabled={!amount}
        >
          다음
        </button>
      </div>
    </div>
  );
};

export default SendAmount;
