import React, { useEffect, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import useAuth from "../../hooks/useAuth";
import { SelectedMemberInfo } from "../../types/ocrTypes";

const Split: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { member } = useAuth(); // 로그인한 사용자 정보 가져오기

  const groupId = location.state?.groupId || null;

  // 로컬 스토리지 및 전달된 데이터
  const [selectedAccount] = useState(() => {
    const savedData = sessionStorage.getItem("selectedAccount");
    return savedData
      ? JSON.parse(savedData)
      : location.state?.selectedAccount || null;
  });

  const [groups, setGroups] = useState<SelectedMemberInfo[]>(() => {
    const savedGroups = sessionStorage.getItem("groups");
    if (savedGroups) return JSON.parse(savedGroups);

    const selectedGroups = location.state?.selectedGroups || [];
    const selectedFriends = location.state?.selectedFriends || [];

    // 친구와 그룹 데이터를 통합
    const combinedMembers = [
      ...selectedGroups.map(
        (groupMember: { memberId: number; memberName: string }) => ({
          memberId: groupMember.memberId,
          name: groupMember.memberName,
          amount: 0,
        })
      ),
      ...selectedFriends.map((friend: { id: number; name: string }) => ({
        memberId: friend.id,
        name: friend.name,
        amount: 0,
      })),
    ];

    // 내 정보 추가
    if (
      member &&
      !combinedMembers.some(
        (m: SelectedMemberInfo) => m.memberId === member.memberId
      )
    ) {
      combinedMembers.unshift({
        memberId: member.memberId,
        name: `(나)${member.name}`,
        amount: 0,
      });
    }

    return combinedMembers;
  });

  const [amount, setAmount] = useState<number | "">(() => {
    const savedAmount = sessionStorage.getItem("totalAmount");
    return savedAmount &&
      savedAmount.trim() &&
      savedAmount.trim() !== "undefined"
      ? JSON.parse(savedAmount)
      : "";
  });

  // 데이터 변경 시 로컬 스토리지 업데이트
  useEffect(() => {
    if (selectedAccount) {
      sessionStorage.setItem(
        "selectedAccount",
        JSON.stringify(selectedAccount)
      );
    }
    // sessionStorage.setItem("selectedAccount", JSON.stringify(selectedAccount));
    sessionStorage.setItem("groups", JSON.stringify(groups));
    sessionStorage.setItem("totalAmount", JSON.stringify(amount));
  }, [selectedAccount, groups, amount]);

  const memberCount = groups.length;

  const handleAmountChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value ? parseFloat(e.target.value) : "";
    setAmount(value);

    if (value && memberCount > 0) {
      const baseAmount = Math.floor(value / memberCount);
      const remainder = value % memberCount;
      const randomIndex = Math.floor(Math.random() * memberCount);

      setGroups(
        groups.map((group: SelectedMemberInfo, index: number) => ({
          ...group,
          amount: baseAmount + (index === randomIndex ? remainder : 0),
        }))
      );
    } else {
      setGroups(
        groups.map((group: SelectedMemberInfo) => ({ ...group, amount: 0 }))
      );
    }
  };

  const handleNext = () => {
    navigate("/settlement/request", {
      state: {
        type: "general",
        selectedAccount,
        groups,
        totalAmount: amount,
        groupId: groupId,
      },
    });
  };

  return (
    <div className="h-full flex flex-col mt-6">
      <div className="flex items-center justify-center mb-5">
        <input
          type="number"
          value={amount || ""}
          onChange={handleAmountChange}
          placeholder="금액을 입력해주세요"
          className="font-bold text-xl border-b border-gray-300 focus:outline-none text-right overflow-hidden"
          style={{
            appearance: "none",
            overflowX: "auto", // 가로 스크롤 허용
          }}
        />
        <span className="ml-2 font-bold text-xl">원</span>
      </div>
      {/* 그룹 리스트 */}
      <div className="flex-grow overflow-y-auto p-6">
        {groups.map((group: SelectedMemberInfo) => (
          <div
            key={group.memberId}
            className="flex justify-between items-center py-4 border-b"
          >
            {/* 사람 이름 */}
            <div className="w-2/5 flex-shrink-0 text-gray-800 overflow-hidden whitespace-nowrap text-ellipsis">
              {group.name}
            </div>
            {/* 금액 */}
            <div
              className="text-gray-800 font-bold text-right flex-grow"
              style={{
                wordBreak: "break-word", // 금액 줄바꿈 처리
              }}
            >
              {group.amount.toLocaleString()} 원
            </div>
          </div>
        ))}
      </div>
      {/* 다음 버튼 */}
      <div className="mt-auto">
        <button
          onClick={handleNext}
          className={`w-full py-2 ${
            groups.every((group) => group.amount === 0)
              ? "bg-gray-300"
              : "bg-[#536DFE]"
          } text-white font-bold rounded-lg`}
          disabled={groups.every((group) => group.amount === 0)}
        >
          다음
        </button>
      </div>
    </div>
  );
};

export default Split;