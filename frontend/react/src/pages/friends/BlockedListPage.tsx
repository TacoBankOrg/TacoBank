import React from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { blockedList, unblockFriend } from "../../api/friendApi";
import backIcon from "../../assets/image/icon/backicon.png";
import { useNavigate } from "react-router-dom";
import { friendReq, listEntry } from "../../types/friendTypes";
import useAuth from "../../hooks/useAuth";
import BottomNav from "../../components/BottomNav";

const BlockedList: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { authData } = useAuth();
  const memberId = authData?.memberId

  // 받은 신청 목록
  const { data: blockedFriends = [], isLoading } = useQuery<listEntry[], Error>(
    {
      queryKey: ["blockedList", memberId],
      queryFn: async (): Promise<listEntry[]> => {
        const response = await blockedList(memberId as number);

        // API 호출 성공 여부를 확인
        if (response.status !== "SUCCESS") {
          throw new Error(response.message || "차단 목록 조회 실패");
        }

        // response.data가 예상한 데이터인지 확인 후 반환
        return response.data as listEntry[];
      },
    }
  );

  // 차단해제
  const unblockFriendMutation = useMutation({
    mutationFn: (dto: friendReq) => unblockFriend(dto),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["blockedList", memberId],
      });
    },
  });

  const handleUnblockFriendRequest = (receiverId: number) => {
    if (!memberId || !receiverId) {
      alert("요청자 또는 친구 정보가 올바르지 않습니다.");
      return;
    }

    const dto: friendReq = {
      requesterId: memberId,
      receiverId: receiverId,
    };

    // useMutation의 mutate 함수 호출
    unblockFriendMutation.mutate(dto);
  };

  return (
    <div id="app-container" className="min-h-screen p-6 pb-20">
      {/* 상단 바 */}
      <header className="relative flex items-center mb-6">
        <button className="absolute left-0 p-1" onClick={() => navigate(-1)}>
          <img src={backIcon} alt="Back" className="w-8 h-8" />
        </button>
        <h1 className="text-3xl font-bold w-full text-center">
          차단
        </h1>
      </header>

      {/* 차단된 친구 목록 */}
      <div className="flex-1 overflow-y-auto pb-20">
        <section className="mb-6">
          <div className="pb-2">
            {isLoading ? (
              <p className="text-sm text-gray-400">로딩 중...</p>
            ) : blockedFriends.length === 0 ? (
              <p className="text-sm text-[#757575]">차단된 친구가 없습니다.</p>
            ) : (
              <ul className="space-y-4">
                {blockedFriends.map((friend) => (
                  <li
                    key={friend.friendId}
                    className="relative flex items-center justify-between"
                  >
                    <span className="font-medium">
                      {friend.friendName}
                    </span>
                    <button
                      onClick={() =>
                        handleUnblockFriendRequest(friend.friendId)
                      }
                      className="px-3 py-2 bg-red-500 text-white rounded-md text-sm hover:bg-red-600 transition-colors"
                    >
                      차단 해제
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </section>
      </div>
      <footer>
        <BottomNav />
      </footer>
    </div>
  );
};

export default BlockedList;
