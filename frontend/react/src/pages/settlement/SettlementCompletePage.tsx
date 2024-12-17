import React from "react";
import { useNavigate } from "react-router-dom";

const SettlementComplete: React.FC = () => {
  const navigate = useNavigate();
  const handleConfirm = () => {
    navigate(`/settlement/receive`); // 받을 돈 정산 현황으로 이동
  };

  return (
    <div id="app-container" className="min-h-screen p-6 pb-20">
      <div className="flex flex-col items-center justify-start mt-60">
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
        <h1 className="text-2xl font-bold text-center leading-relaxed">
          정산 요청이 <br /> 완료되었어요
        </h1>
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
    </div>
  );
};

export default SettlementComplete;
