import React, { useState, useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import Split from "./SplitPage";
import Receipt from "./ReceiptPage";
import backIcon from "../../assets/image/icon/backicon.png";

const SettlementOptions: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();

  // 이전 페이지에서 전달된 currentTab을 기본값으로 설정
  const [currentTab, setCurrentTab] = useState<"general" | "receipt">(
    location.state?.currentTab || "general"
  );

  const handleTabChange = (tab: "general" | "receipt") => {
    setCurrentTab(tab);
  };

  const handleBack = () => {
    const confirmBack = window.confirm(
      "이전 화면으로 돌아가면 입력한 내용이 사라집니다. 그래도 돌아가시겠습니까?"
    );
    if (confirmBack) {
      if (typeof sessionStorage !== "undefined") {
        sessionStorage.removeItem("groups");
        sessionStorage.removeItem("totalAmount");
        sessionStorage.removeItem("receiptItems");
        sessionStorage.removeItem("updatedReceiptItems");
        sessionStorage.removeItem("receiptId");
        // selectedAccount는 삭제하지 않음
      }
      navigate("/settlement/memberselect");
    } else {
      window.history.pushState(null, document.title, window.location.href);
    }
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
      <header className="relative flex items-center mb-6">
        <button className="absolute left-0 p-1" onClick={handleBack}>
         <img src={backIcon} alt="Back" className="w-8 h-8" />
        </button>
        <h1 className="text-3xl font-bold w-full text-center">정산 선택</h1>
      </header>

      {/* 상단 탭 */}
      <div className="flex justify-center">
        <button
          className={`flex-1 py-2 text-center ${
            currentTab === "general" ? "text-[#536DFE] border-b-2 border-[#536DFE] font-bold" : ""
          }`}
          onClick={() => handleTabChange("general")}
        >
          1/N
        </button>
        <button
          className={`flex-1 py-2 text-center ${
            currentTab === "receipt" ? "text-[#536DFE] border-b-2 border-[#536DFE] font-bold" : ""
          }`}
          onClick={() => handleTabChange("receipt")}
        >
          영수증
        </button>
       
      </div>

      {/* 탭에 따른 컴포넌트 렌더링 */}
      <div >
        {currentTab === "general" && <Split />}
        {currentTab === "receipt" && <Receipt />}
        
      </div>
      
    </div>
  );
  
};

export default SettlementOptions;