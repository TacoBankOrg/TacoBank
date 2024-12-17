import { Outlet, useNavigate, useLocation } from "react-router-dom";
import backIcon from "../../../assets/image/icon/backicon.png";
import BottomNav from "../../../components/BottomNav";
import { statusSettlement } from "../../../api/settlementApi";
import { useQuery } from "@tanstack/react-query";
import {
  CreatedSettlement,
  IncludedSettlement,
} from "../../../types/settlementTypes";
import useAuth from "../../../hooks/useAuth";

const Settlement = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { member } = useAuth();
  const memberId = member?.memberId;

  const isReceiveOrSend =
    location.pathname === "/settlement/receive" ||
    location.pathname === "/settlement/send";

  const {
    data: settlementsData,
    isLoading,
    isError,
  } = useQuery<{
    createdSettlements: CreatedSettlement[];
    includedSettlements: IncludedSettlement[];
  }>({
    queryKey: ["settlements", memberId],
    queryFn: async () => {
      const response = await statusSettlement(memberId as number);
      if (!response.data || response.status !== "SUCCESS") {
        throw new Error(response.message || "조회 실패");
      }
      // 응답 데이터를 명확히 구분해서 반환
      return {
        createdSettlements: response.data.createdSettlements || [],
        includedSettlements: response.data.includedSettlements || [],
      };
    },

    enabled: isReceiveOrSend && !!memberId, // 특정 조건에서만 실
  });

  const createdSettlements = settlementsData?.createdSettlements || [];
  const includedSettlements = settlementsData?.includedSettlements || [];

  // "/settlement/receive"와 "/settlement/send" 경로인지 확인

  return (
    <>
      {isReceiveOrSend ? (
        // 정산현황(받을 돈 / 보낼 돈) 전용 레이아웃
        <div id="app-container" className="min-h-screen p-6 pb-20">
          <header className="relative flex items-center justify-between mb-6">
            <button className="p-1" onClick={() => navigate("/")}>
              <img src={backIcon} alt="Back" className="w-8 h-8" />
            </button>
            <h1 className="text-3xl font-bold flex-grow text-center">
              정산현황
            </h1>
            <button
              className="p-1 text-white text-sm bg-[#485acf] rounded-lg"
              onClick={() => navigate("/settlement/memberselect")}
            >
              더치페이
            </button>
          </header>

          {/* 탭 버튼 */}
          <div className="flex justify-center mb-4">
            <button
              onClick={() => navigate("/settlement/receive")}
              className={`flex-1 py-2 text-center font-bold ${
                location.pathname === "/settlement/receive"
                  ? "text-[#485acf] border-b-2 border-[#485acf]"
                  : "text-gray-500"
              }`}
            >
              받을 돈
            </button>
            <button
              onClick={() => navigate("/settlement/send")}
              className={`flex-1 py-2 text-center font-bold ${
                location.pathname === "/settlement/send"
                  ? "text-[#485acf] border-b-2 border-[#485acf]"
                  : "text-gray-500"
              }`}
            >
              보낼 돈
            </button>
          </div>

          {/* Outlet: 하위 컴포넌트 렌더링 */}
          {!isLoading && !isError && (
            <Outlet
              context={{
                includedSettlements,
                createdSettlements,
              }}
            />
          )}

          {/* 하단 네비게이션 */}
          <footer>
            <BottomNav />
          </footer>
        </div>
      ) : (
        // 다른 Settlement 하위 경로에서는 Outlet만 렌더링
        <div>
          <Outlet />
        </div>
      )}
    </>
  );
};

export default Settlement;
