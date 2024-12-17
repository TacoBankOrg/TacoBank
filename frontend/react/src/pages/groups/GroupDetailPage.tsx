import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import backIcon from "../../assets/image/icon/backicon.png";
import {
  deleteGroup,
  expelGroupMember,
  groupList,
  leaveGroup,
} from "../../api/groupApi";
import { Group, GroupDetails, GroupMember } from "../../types/groupTypes.ts";
import useAuth from "../../hooks/useAuth.ts";
import BottomNav from "../../components/BottomNav.tsx";

const GroupDetail: React.FC = () => {
  const { groupId } = useParams<{ groupId: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { authData } = useAuth();
  const memberId = authData?.memberId

  const [groupDetails, setGroupDetails] = useState<GroupDetails | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [actionType, setActionType] = useState<"삭제" | "탈퇴" | "추방" | null>(
    null
  );
  const [selectedMember, setSelectedMember] = useState<GroupMember | null>(
    null
  );
  const deleteGroupMutation = useMutation({
    mutationFn: (groupId: number) => deleteGroup(Number(memberId), groupId),
    onSuccess: () => {
      // groupList 키를 가진 모든 캐시를 무효화
      queryClient.invalidateQueries({
        queryKey: ["groupDetails"],
      });
      alert("그룹이 삭제되었습니다.");
      navigate("/groups");
    },
    onError: (error) => {
      console.error("그룹 삭제 중 에러:", error);
    },
  });

  // 그룹 탈퇴
  const leaveGroupMutation = useMutation({
    mutationFn: (groupId: number) => leaveGroup(Number(memberId), groupId),

    onSuccess: () => {
      // groupList 키를 가진 모든 캐시를 무효화
      queryClient.invalidateQueries({
        queryKey: ["groupDetails"],
      });
      alert("그룹을 탈퇴했습니다.");
      navigate("/groups");
    },

    onError: (error) => {
      console.error("그룹 탈퇴 중 에러:", error);
    },
  });

  // 그룹 상세 정보 가져오기
  useEffect(() => {
    const fetchGroupDetails = async (): Promise<void> => {
      try {
        const response = await groupList(Number(memberId));
        const groups = response.data;

        const group = groups.find((g: Group) => g.groupId === Number(groupId));

        if (group) {
          setGroupDetails(group);
        } else {
          setGroupDetails(null);
          alert("그룹 정보를 불러올 수 없습니다.");
          navigate("/groups");
        }
        setIsLoading(false);
      } catch (error) {
        console.error("Error fetching group details:", error);
      }
    };

    fetchGroupDetails();
  }, [groupId, memberId]);

  // 멤버 추방 Mutation
  const expelMutation = useMutation({
    mutationFn: (targetMemberId: number) => expelGroupMember(Number(memberId), Number(groupId), targetMemberId),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["groupDetails"],
      });
      alert("멤버를 성공적으로 추방했습니다.");
      setGroupDetails((prev) => {
        if (!prev) return null;
        const updatedMembers = prev.members.filter(
          (member) => member.memberId !== selectedMember?.memberId
        );
        return { ...prev, members: updatedMembers };
      });
    },
    onError: (error) => {
      console.error("멤버 추방 중 에러:", error);
    },
  });

  if (isLoading) return <div>로딩 중...</div>;
  if (!groupDetails) return <div>그룹 정보를 불러올 수 없습니다.</div>;

  const handleConfirmAction = () => {
    if (memberId === undefined) {
      alert("로그인 상태가 아닙니다.");
      return;
    }
    if (actionType === "삭제") {
      deleteGroupMutation.mutate(Number(groupId));
    } else if (actionType === "탈퇴") {
      leaveGroupMutation.mutate(Number(groupId));
    } else if (actionType === "추방" && selectedMember) {
      expelMutation.mutate(selectedMember.memberId);
    }
    setShowConfirmModal(false);
    setActionType(null);
  };

  // 리더와 일반 멤버 구분
  const leader = {
    memberId: groupDetails.leaderId,
    memberName: groupDetails.leaderName,
  };

  const otherMembers = groupDetails.members.filter(
    (member: GroupMember) => member.memberId !== groupDetails.leaderId
  );

  return (
    <div id="app-container" className="min-h-screen p-6 pb-20">
      <header className="relative flex items-center justify-between">
        <button className="p-1" onClick={() => navigate(-1)}>
          <img src={backIcon} alt="Back" className="w-8 h-8" />
        </button>
        <h1 className="text-3xl font-bold flex-grow text-center">
          {groupDetails.groupName}
        </h1>
        <button
          onClick={() => {
            setActionType(groupDetails.leaderId === memberId ? "삭제" : "탈퇴");
            setShowConfirmModal(true);
          }}
          className="text-red-500 px-2 rounded-md border border-red-500"
        >
          {groupDetails.leaderId === memberId ? "삭제" : "탈퇴"}
        </button>
      </header>

      {/* 멤버 목록 */}
      <section className="flex-grow mt-4">
        <h2 className="text-xl font-bold mb-2">멤버 목록</h2>
        <div className="overflow-y-auto bg-white rounded p-4">
          {/* 리더 표시 */}
          {leader && (
            <div className="relative flex items-center justify-between p-4 border-b">
              <span className="font-medium text-gray-800">
                {leader.memberName} (리더)
              </span>
            </div>
          )}
          {/* 일반 멤버 표시 */}
          {otherMembers.map((member: GroupMember) => (
            <div
              key={member.memberId}
              className="relative flex items-center justify-between p-4 border-b last:border-b-0"
            >
              <span className="font-medium text-gray-800">
                {member.memberName}
              </span>
              {groupDetails.leaderId === memberId && (
                <button
                  onClick={() => {
                    setActionType("추방");
                    setSelectedMember(member);
                    setShowConfirmModal(true);
                  }}
                  className="text-white px-2 rounded-md border bg-red-500"
                >
                  추방
                </button>
              )}
            </div>
          ))}
        </div>
      </section>
      {showConfirmModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-20">
          <div className="bg-white rounded-lg p-6 w-80">
            <p className="text-gray-800 font-medium text-lg">
              {actionType === "추방" && selectedMember
                ? `${selectedMember.memberName}님을 추방하시겠습니까?`
                : `그룹을 ${actionType}하시겠습니까?`}
            </p>
            <div className="flex justify-end mt-4 space-x-4">
              <button
                className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300"
                onClick={() => {
                  setShowConfirmModal(false);
                  setSelectedMember(null);
                }}
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

export default GroupDetail;