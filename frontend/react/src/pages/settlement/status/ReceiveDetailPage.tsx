import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import backIcon from "../../../assets/image/icon/backicon.png";
import { detailSettlement, notifySettlement } from "../../../api/settlementApi";
import { SettlementDetailResponse } from "../../../types/settlementTypes";
import useAuth from "../../../hooks/useAuth.ts";
import BottomNav from "../../../components/BottomNav.tsx";

const SettlementDetails: React.FC = () => {
  const navigate = useNavigate();
  const { settlementId } = useParams<{ settlementId: string }>();
  const [settlementDetailsData, setSettlementDetailsData] =
    useState<SettlementDetailResponse | null>(null);
  const [loading, setLoading] = useState(false);

  const { member } = useAuth();
  const memberId = member?.memberId

  useEffect(() => {
    const fetchSettlementDetails = async () => {
      try {
        if (!settlementId) return;
        const response = await detailSettlement(memberId as number, Number(settlementId));
        setSettlementDetailsData(response.data);
        console.log(response);
      } catch (error) {
        console.error("Failed to fetch settlement details:", error);
      }
    };

    fetchSettlementDetails();
  }, [settlementId]);

  const handleNotify = async (groupMemberId: number) => {
    try {
      setLoading(true);
      if (!settlementId) return;
  
      // groupMemberId를 memberId로 사용하여 요청
      await notifySettlement(groupMemberId, Number(settlementId));
  
      // groupMemberId에 맞는 그룹 멤버 이름 찾기
      const memberName = settlementDetailsData?.settlementDetailsList.find(
        (member) => member.groupMemberId === groupMemberId
      )?.groupMemberName;
  
      if (memberName) {
        alert(`${memberName}에게 독촉 메시지를 성공적으로 보냈습니다.`);
      } else {
        alert("그룹 멤버를 찾을 수 없습니다.");
      }
    } catch (error) {
      console.error("Failed to notify member:", error);
      alert("독촉 메시지를 보내는 데 실패했습니다. 다시 시도해주세요.");
    } finally {
      setLoading(false);
    }
  };

  // 미정산 및 정산완료 데이터 필터링
  const unsettledMembers =
    settlementDetailsData?.settlementDetailsList.filter(
      (member) => member.groupMemberStatus === "N"
    ) || [];
  const settledMembers =
    settlementDetailsData?.settlementDetailsList.filter(
      (member) => member.groupMemberStatus === "Y"
    ) || [];

  return (
    <div id="app-container" className="min-h-screen p-6 pb-20">
      {/* 헤더 */}
      <header className="relative flex items-center mb-6">
        <button className="absolute left-0 p-1" onClick={() => navigate(-1)}>
          <img src={backIcon} alt="Back" className="w-8 h-8" />
        </button>
        <h1 className="text-3xl font-bold w-full text-center">
          {"정산 상세"}
        </h1>
      </header>

      {/* 미정산 멤버 */}
      {unsettledMembers.length > 0 && (
        <>
          <h2 className="text-m font-semibold mb-3">미정산</h2>
          <div className="border-b border-gray-200">
            <div className="space-y-2">
              {unsettledMembers.map((member) => (
                <div
                  key={member.groupMemberId}
                  className="p-4 flex justify-between items-center"
                >
                  <div>
                    <p className="font-medium">
                      {member.groupMemberName}
                    </p>
                    <span className="text-lg font-bold">
                      {member.groupMemberAmount.toLocaleString()}원
                    </span>
                  </div>
                  <button
                    onClick={() => handleNotify(member.groupMemberId)}
                    className="px-4 py-2 bg-[#536DFE] text-white text-sm rounded-lg hover:bg-[#485acf]"
                    disabled={loading}
                  >
                    {loading ? "독촉 중..." : "독촉하기"}
                  </button>
                </div>
              ))}
            </div>
          </div>
        </>
      )}


      {/* 정산 완료 멤버 */}
      {settledMembers.length > 0 && (
          <>
            <h2 className="text-m font-semibold mt-6 mb-3">
              정산 완료
            </h2>
            <div className="space-y-2">
              {settledMembers.map((member) => (
                  <div
                      key={member.groupMemberId}
                      className="rounded-lg p-4 flex justify-between items-center"
                  >
                    <div>
                      <p className="font-medium">
                        {settlementDetailsData?.settlementDetailsList.find(
                            (detail) => detail.groupMemberId === member.groupMemberId
                        )?.leaderId === member.groupMemberId
                            ? `${member.groupMemberName} (나)`
                            : member.groupMemberName}
                      </p>
                      <span className="text-lg font-bold">
                  {member.groupMemberAmount.toLocaleString()}원
                </span>
                    </div>
                    <div className="text-sm text-gray-500">
                      {member.updatedDate
                          ? `${new Date(member.updatedDate).getDate()}일`
                          : "정산 날짜 정보 없음"}
                    </div>
                  </div>
              ))}
            </div>
          </>
      )}

      {/* 데이터 로드 실패 또는 빈 상태 처리 */}
      {!settlementDetailsData && (
        <div className="text-center text-[#757575]">
          데이터를 불러오는 중입니다...
        </div>
      )}
      {settlementDetailsData?.settlementDetailsList.length === 0 && (
        <div className="text-center text-[#757575]">
          표시할 정산 데이터가 없습니다.
        </div>
      )}
      <footer>
            <BottomNav />
          </footer>
    </div>
  );
};

export default SettlementDetails;
