import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import backIcon from "../../assets/image/icon/backicon.png";
import BottomNav from "../../components/BottomNav";
import {
  acceptGroup,
  deleteGroup,
  leaveGroup,
  recvList,
  rejectGroup,
} from "../../api/groupApi";
import { Group, GroupInvitation } from "../../types/groupTypes";
import { useGroupList } from "../../hooks/useGroupList.ts";
import useAuth from "../../hooks/useAuth";

const GroupList: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const [selectedGroup, setSelectedGroup] = useState<Group | null>(null);
  const [actionType, setActionType] = useState<"삭제" | "탈퇴" | null>(null);
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const { authData } = useAuth();
  const memberId = authData?.memberId
  const { data: groups = [], isLoading: isGroupsLoading } =
    useGroupList(Number(memberId));

  // 받은 신청 목록 가져오기
  const { data: invitations = [], isLoading: inviteLoading } = useQuery<
    GroupInvitation[]
  >({
    queryKey: ["recvList", memberId],
    queryFn: async (): Promise<GroupInvitation[]> => {
      const response = await recvList(Number(memberId));
      if (!response.data || response.status !== "SUCCESS") {
        throw new Error(response.message || "받은 신청 그룹 목록 조회 실패");
      }
      return response.data as GroupInvitation[];
    },
  });

  // 그룹 삭제
  const deleteGroupMutation = useMutation({
    mutationFn: (groupId: number) => deleteGroup(Number(memberId), groupId),
    onSuccess: () => {
      // groupList 키를 가진 모든 캐시를 무효화
      queryClient.invalidateQueries({
        queryKey: ["groupList"],
      });
    },
  });

  // 그룹 탈퇴
  const leaveGroupMutation = useMutation({
    mutationFn: (groupId: number) => leaveGroup(Number(memberId), groupId),
    onSuccess: () => {
      // groupList 키를 가진 모든 캐시를 무효화
      queryClient.invalidateQueries({
        queryKey: ["groupList"],
      });
    },
  });

  // 그룹 초대 수락
  const acceptGroupMutation = useMutation({
    mutationFn: (groupId: number) => acceptGroup(Number(memberId), groupId),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["recvList", memberId], // 수락된 초대 리스트 무효화
      });
      queryClient.invalidateQueries({
        queryKey: ["groupList", memberId], // 그룹 리스트 무효화
      });
    },
  });

  // 그룹 초대 거절
  const rejectGroupMutation = useMutation({
    mutationFn: (groupId: number) => rejectGroup(Number(memberId), groupId),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["recvList", memberId], // 거절된 초대 리스트 무효화
      });
    },
  });

  const handleConfirmAction = () => {
    if (memberId === undefined) {
      alert("로그인 상태가 아닙니다.");
      return;
    }
    if (selectedGroup) {
      if (actionType === "삭제") {
        deleteGroupMutation.mutate(selectedGroup.groupId);
      } else if (actionType === "탈퇴") {
        leaveGroupMutation.mutate(selectedGroup.groupId);
      }
    }
    setShowConfirmModal(false);
    setActionType(null);
    setSelectedGroup(null);
  };

  return (
    <div id="app-container" className="min-h-screen p-6 pb-20">
      {/* 상단 바 */}
      <header className="relative flex items-center justify-between mb-6">
        <button className="p-1" onClick={() => navigate("/")}>
          <img src={backIcon} alt="Back" className="w-8 h-8" />
        </button>
        <h1 className="text-3xl font-bold flex-grow text-center">
          그룹
        </h1>
        <button
          className="p-1 text-white text-sm bg-[#536DFE] hover:bg-[#485acf] rounded-lg"
          onClick={() => navigate("/create-group")}
        >
          만들기
        </button>
      </header>

      {/* 받은 신청 섹션 */}
      <section className="mb-8">
        <h2 className="text-m font-semibold mb-2">받은 신청</h2>
        {inviteLoading ? (
          <p>로딩 중...</p>
        ) : invitations.length === 0 ? (
          <p className="text-sm text-[#757575]">받은 신청이 없습니다.</p>
        ) : (
          invitations.map((invite, index) => (
            <div
            key={`${invite.groupId}-${index}`} 
              className="flex items-center justify-between p-4 rounded-lg mb-2"
            >
              <span className="font-medium text-gray-800">
                {invite.groupName}
              </span>
              <div className="flex gap-2">
                <button
                  className="px-3 py-1 bg-[#536DFE] text-white rounded-md text-sm hover:bg-[#485acf]"
                  onClick={() => acceptGroupMutation.mutate(invite.groupId)}
                >
                  수락
                </button>
                <button
                  className="px-3 py-1 bg-red-500 text-white rounded-md text-sm hover:bg-red-600"
                  onClick={() => rejectGroupMutation.mutate(invite.groupId)}
                >
                  거절
                </button>
              </div>
            </div>
          ))
        )}
      </section>

      {/* 그룹 섹션 */}
      <section>
        <h2 className="text-m font-semibold text-gray-700 mb-2">그룹</h2>
        {isGroupsLoading ? (
          <p>로딩 중...</p>
        ) : groups.length === 0 ? (
          <p className="text-sm text-gray-400">그룹이 없습니다.</p>
        ) : (
          groups.map((group, index) => (
            <div
            key={`${group.groupId}-${index}`}
              className="relative flex items-center justify-between p-4 rounded-lg mb-2 cursor-pointer"
              onClick={() => navigate(`/groups/${group.groupId}`)} // div 클릭 시 상세 페이지 이동
            >
              <span className="font-medium">
                {group.groupName}
              </span>
              <div className="flex gap-2">
                <button
                  className="px-3 py-1 bg-red-500 text-white rounded-md text-sm hover:bg-red-600 hover"
                  onClick={(e) => {
                    e.stopPropagation(); // 부모 div의 클릭 이벤트 방지
                    setActionType(
                      group.leaderId === memberId ? "삭제" : "탈퇴"
                    );
                    setSelectedGroup(group);
                    setShowConfirmModal(true);
                  }}
                >
                  {group.leaderId === memberId ? "삭제" : "탈퇴"}
                </button>
              </div>
            </div>
          ))
        )}
      </section>

      {/* 확인 모달 */}
      {showConfirmModal && selectedGroup && (
        <div className="absolute inset-0 bg-black bg-opacity-50 flex items-center justify-center z-20">
          <div className="bg-white rounded-lg p-6 w-80">
            <p className="font-medium text-lg">
              {selectedGroup.groupName} {actionType}하시겠습니까?
            </p>
            <div className="flex justify-end mt-4 space-x-4">
              <button
                className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300"
                onClick={() => setShowConfirmModal(false)}
              >
                취소
              </button>
              <button
                className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600"
                onClick={handleConfirmAction}
              >
                확인
              </button>
            </div>
          </div>
        </div>
      )}
     <footer>
        <BottomNav />
      </footer>
    </div>
  );
};

export default GroupList;
