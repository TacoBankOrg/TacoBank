import React, { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { bankData } from "../../data/bankData";
import { FavoriteAccountReq } from "../../types/accountTypes";
import useAuth from "../../hooks/useAuth";
import { favoriteAccountSelect, favoriteAccountList } from "../../api/accountApi";
import { handleApiError } from "../../utils/apiHelpers";
import axios from "axios";
import { useQueryClient } from "@tanstack/react-query";

const TransferSuccess: React.FC = () => {
  const [isModalOpen, setModalOpen] = useState(false);
  const queryClient = useQueryClient();
  const { member } = useAuth();
  const memberId = member?.memberId;
  const navigate = useNavigate();
  const location = useLocation();
  const { bankCode, accountNum, accountHolder, amount } = location.state || {}; // 데이터 수신

  useEffect(() => {
    if (!location.state ) {
      // state가 없으면 이전 페이지로 리다이렉트
      navigate("/");
    }
  }, [location.state , navigate]);

  if (!location.state ) {
    // state가 없으면 아무것도 렌더링하지 않음
    return null;
  }

  const handleConfirm = async () => {
    try {
      const response = await favoriteAccountList(memberId as number);
      const favoriteAccounts = response.data.map(
        (account: { accountNum: string }) => account.accountNum
      );

      if (favoriteAccounts.includes(accountNum)) {
        sessionStorage.removeItem("amount");
        navigate("/"); // 홈으로 이동
      } else {
        // 없는 경우 모달 열기
        setModalOpen(true);
      }
    } catch (error) {
      console.error("즐겨찾기 계좌 확인 실패:", error);
      alert("오류가 발생했습니다. 다시 시도해주세요.");
      sessionStorage.removeItem("amount");
      navigate("/"); // 홈으로 이동
    }
  };

  const handleAddToFavorites = async () => {
    try {
      const dto: FavoriteAccountReq = {
        memberId: memberId as number, // 사용자의 멤버 ID
        accountNum, // 계좌 번호
      };

      // 즐겨찾기 추가 API 호출
      const response = await favoriteAccountSelect(dto);
      if (response.status === "SUCCESS") {
        alert("즐겨찾기에 등록되었습니다!");
        sessionStorage.removeItem("amount");
        // 캐시 무효화
        queryClient.invalidateQueries({
          queryKey: ["favoriteAccounts", memberId],
        });
      } else {
        throw new Error("즐겨찾기 등록에 실패했습니다.");
      }

      setModalOpen(false); // 모달 닫기
      navigate("/"); // 홈으로 이동
    } catch (error) {
      if (axios.isAxiosError(error)) {
        const errorMessage = handleApiError(error); // 에러 메시지 처리
        alert(errorMessage);
      } else {
        console.error("Error:", error);
      }
    }
  };

  const handleCloseModal = () => {
    setModalOpen(false); // 모달 닫기
    sessionStorage.removeItem("amount");
    navigate("/"); // 홈으로 이동
  };

  const getBankName = (bankCode: string): string => {
    const bank = bankData.find((bank) => bank.bankCode === bankCode);
    return bank ? bank.bankName : "알 수 없는 은행";
  };

  return (
    <div id="app-container" className="min-h-screen flex flex-col p-6">
      <div className="flex flex-col items-center justify-start mt-60">
        {/* 아이콘 */}
        <div className="w-20 h-20 bg-[#536DFE] rounded-full flex items-center justify-center mb-6">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            className="h-16 w-15 text-white"
            fill="none"
            viewBox="0 0 22 22"
            stroke="currentColor"
            strokeWidth={2}
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              d="M5 13l4 4L19 7"
            />
          </svg>
        </div>

        <div className="text-center mb-6">
          <p className="text-2xl font-medium">
            <span className="font-medium">{accountHolder}</span>
            님에게
          </p>
          <div className="flex items-center justify-center space-x-1 font-medium text-2xl">
            <p className="text-[#536DFE]">{amount}원</p>
            <p>보냈어요</p>
          </div>
          <p className="text-sm text-[#757575] mt-2">
            {getBankName(bankCode)} {accountNum}
          </p>
        </div>
      </div>

      {/* 확인 버튼 */}
      <div className="mt-auto">
        <button
          onClick={handleConfirm}
          className="w-full py-2 bg-[#536DFE] text-white font-bold rounded-lg hover:bg-[#485acf]"
        >
          확인
        </button>
      </div>
      {isModalOpen && (
        <div className="absolute inset-0 bg-black bg-opacity-50 flex items-center justify-center z-20">
          <div className="bg-white rounded-lg p-6 w-80">
            <h2 className="text-gray-800 font-medium text-lg">즐겨찾기 등록</h2>
            <p className="text-sm text-gray-600 mt-4">
              해당 계좌를 즐겨찾기에 등록하시겠습니까?
            </p>
            <div className="flex justify-end mt-6 space-x-4">
              <button
                className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300"
                onClick={handleCloseModal}
              >
                아니오
              </button>
              <button
                className="px-4 py-2 bg-[#536DFE] text-white rounded hover:bg-[#485acf]"
                onClick={handleAddToFavorites}
              >
                네
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default TransferSuccess;