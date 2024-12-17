import React from "react";
import { useNavigate, useOutletContext } from "react-router-dom";
// import { useQuery } from "@tanstack/react-query";
// import { statusSettlement } from "../../../api/settlementApi";
import { CreatedSettlement } from "../../../types/settlementTypes";
import BottomNav from "../../../components/BottomNav";

const ReceiveMoney: React.FC = () => {
  const navigate = useNavigate();

  // 정산 데이터 가져오기
  const { createdSettlements } = useOutletContext<{
    createdSettlements: CreatedSettlement[];
  }>();

  const getYearAndMonth = (date: string) => {
    const parsedDate = new Date(date);
    const year = String(parsedDate.getFullYear()).slice(2); // 2024 -> 24
    const month = parsedDate.getMonth() + 1; // 0-indexed, so add 1
    return `${year}년 ${month}월`;
  };

  const getDayFromDate = (date: string | null) => {
    if (!date) return null;
    const parsedDate = new Date(date);
    return parsedDate.getDate(); // day 부분만 추출
  };

  const handleCardClick = (settlementId: number) => {
    // 상세 페이지로 이동
    navigate(`/settlement/details/${settlementId}`);
  };

  return (
    <div
          className="flex flex-col gap-4 overflow-y-auto"
          style={{
            maxHeight: "calc(100vh - 72px)", // 하단 네비게이션바 높이 제외
          }}
        >
        {createdSettlements.length > 0 ? (
            createdSettlements
                .sort((a) => (a.totalStatus === "Y" ? 1 : -1)) // 정산 완료된 항목(Y)을 뒤로 정렬
                .map((item) => (
                    <div key={item.settlementId} className="mb-4">
                      {/* 정산 완료된 항목만 날짜 표시 */}
                      {item.totalStatus === "Y" && (
                          <p className="text-sm text-gray-500 mb-1">
                            {getYearAndMonth(item.createdDate)}
                          </p>
                      )}
                      {/* 카드 본체를 흐리게 처리 */}
                      <div
                          onClick={() => handleCardClick(item.settlementId)}
                          className={`p-4 rounded-lg ${
                              item.totalStatus === "Y" ? "bg-[#EAEDFF] opacity-50" : "bg-[#EAEDFF]"
                          }`}
                      >
                        <div className="flex justify-between items-start">
                          <div>
                            <p className="text-gray-800 font-bold">{item.groupName}</p>
                            <p className="text-lg font-bold text-gray-800">
                              {item.totalAmount.toLocaleString()}원
                            </p>
                          </div>
                        </div>
                        <div className="flex justify-between items-center text-sm text-gray-600 mt-2">
                          <p>{item.totalStatus === "Y" ? "정산 완료" : "정산 진행중"}</p>
                          {item.totalStatus === "Y" && item.completedDate && (
                              <p>{getDayFromDate(item.completedDate)}일</p>
                          )}
                        </div>
                      </div>
                    </div>
                ))
        ) : (
            <div className="text-center text-[#757575]">표시할 정산 데이터가 없습니다.</div>
        )}
        <footer>
            <BottomNav />
          </footer>
      </div>
  );
};

export default ReceiveMoney;