import React, { useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import backIcon from "../../assets/image/icon/backicon.png";
import { requestSettlement } from "../../api/settlementApi";
import useAuth from "../../hooks/useAuth";
import { SettlementRequestDto } from "../../types/settlementTypes";
import { SelectedMemberInfo } from "../../types/ocrTypes";
import axios from "axios";

const SettlementReq: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { member } = useAuth();
  const memberId = member?.memberId
  
  // 전달받은 데이터
  const { type, totalAmount, groups, selectedAccount, groupId, receiptId, productMemberDetails  } = location.state || {
    type: "",
    totalAmount: 0,
    groups: [],
    selectedAccount: null,
    groupId: null,
    receiptId: 0,
    productMemberDetails: null,
  };
  useEffect(() => {
    if (!location.state) {
      // 데이터가 없으면 이전 페이지로 리다이렉트
      navigate("/"); // -1은 브라우저 기록을 기반으로 이전 페이지로 이동
    }
  }, [location.state, navigate]);

  const handleRequest = async () => {
    try {
      if (!totalAmount || totalAmount <= 0) {
        alert("정산 총액은 0원 이상이어야 합니다.");
        handleBack();
      }

      const requestData: SettlementRequestDto = {
        type: type,
        // TODO 확인 필요
        leaderId: memberId as number,
        groupId: groupId,
        settlementAccountId: selectedAccount?.accountId,
        totalAmount: totalAmount!,
        friendIds: groups.map(
          (selectedMember: SelectedMemberInfo) => selectedMember.memberId
        ),
        memberAmounts: groups.map((selectedMember: SelectedMemberInfo) => ({
          memberId: selectedMember.memberId,
          amount: selectedMember.amount,
        })),
        receiptId: type === "receipt" ? receiptId : undefined, // 선택적 필드
        productMemberDetails:
          type === "receipt" ? productMemberDetails : undefined, // 선택적 필드
      };

      console.log("Request data:", requestData); // 디버깅용
      const response = await requestSettlement(requestData);

      alert("요청이 완료되었습니다!");

      // 로컬 스토리지에서 정산 관련 데이터 삭제
      sessionStorage.removeItem("selectedAccount");
      sessionStorage.removeItem("groups");
      sessionStorage.removeItem("totalAmount");
      sessionStorage.removeItem("receiptId");
      sessionStorage.removeItem("receiptItems");
      sessionStorage.removeItem("updatedReceiptItems");

      navigate("/settlement/complete", { state: { response } });
    } catch (error) {
      console.error("정산 요청 실패:", error);
      let errMsg = "정산 요청에 실패했습니다. 다시 시도해주세요.";

      if (axios.isAxiosError(error)) {
        // 서버에서 내려준 response가 있는지 확인
        if (error.response) {
          console.error("정산 요청 실패:", error.response.data); // 서버의 응답 데이터
          if (error.response.data.message) errMsg = error.response.data.message;
        }
      } else {
        console.error("정산 요청 실패:", error); // axios 외의 다른 오류
        alert("정산 요청 중 알 수 없는 오류가 발생했습니다.");
      }

      alert(errMsg);
    }
  };

  const handleBack = () => {
    navigate("/settlement/action", { state: { currentTab: type } });
  };

  // 브라우저 뒤로가기 감지
  useEffect(() => {
    const handlePopState = () => {
      handleBack(); // 뒤로가기 버튼 클릭 시 handleBack 실행
    };

    // popstate 이벤트 리스너 추가
    window.addEventListener("popstate", handlePopState);

    // 컴포넌트 언마운트 시 리스너 제거
    return () => {
      window.removeEventListener("popstate", handlePopState);
    };
  }, []);

  return (
    <div id="app-container" className="min-h-screen p-6 pb-20">
      {/* 상단 바 */}
      <header className="relative flex items-center mb-6">
        <button className="absolute left-0 p-1" onClick={handleBack}>
          <img src={backIcon} alt="Back" className="w-8 h-8" />
        </button>
        <h1 className="text-3xl font-bold w-full text-center">
          더치페이
        </h1>
      </header>

      {/* 총 금액 */}
      <div className="flex flex-col items-center mb-6">
        <h2 className="text-xl text-center font-bold">
          총 {Number(totalAmount).toLocaleString()}원
        </h2>
      </div>

      {/* 멤버 리스트 */}
      <div className="flex-1 divide-gray-300">
        {groups.map((group: SelectedMemberInfo) => (
          <div
            key={group.memberId} // 'memberId'를 키로 사용
            className="flex justify-between items-center py-4"
          >
            {/* 멤버 이름 */}
            <div>{group.name}</div>

            {/* 멤버 금액 */}
            <div className="font-bold">
              {group.amount.toLocaleString()} 원
            </div>
          </div>
        ))}
      </div>

      {/* 요청하기 버튼 */}
      <div className="mt-auto">
        <button
          onClick={handleRequest}
          className="w-full py-3 bg-[#536DFE] text-white font-bold rounded-lg hover:bg-[#485acf]"
        >
          요청하기
        </button>
      </div>
    </div>
  );
};

export default SettlementReq;