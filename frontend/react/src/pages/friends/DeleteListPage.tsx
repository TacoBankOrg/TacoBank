import React from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { deleteFriendList, undoDeletedFriend } from "../../api/friendApi";
import backIcon from "../../assets/image/icon/backicon.png";
import { useNavigate } from "react-router-dom";
import { friendReq, listEntry } from "../../types/friendTypes";
import useAuth from "../../hooks/useAuth";
import BottomNav from "../../components/BottomNav";

const DeleteList: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { authData } = useAuth();
  const memberId = authData?.memberId

  // 삭제 친구 목록
  const { data: deletedFriends = [], isLoading } = useQuery<listEntry[], Error>(
    {
      queryKey: ["deletedList", memberId],
      queryFn: async (): Promise<listEntry[]> => {
        const response = await deleteFriendList(memberId as number);

        // API 호출 성공 여부를 확인
        if (response.status !== "SUCCESS") {
          throw new Error(response.message || "차단 목록 조회 실패");
        }

        // response.data가 예상한 데이터인지 확인 후 반환
        return response.data as listEntry[];
      },
    }
  );

  // 삭제해제
  const undoDeletedFriendMutation = useMutation({
    mutationFn: (dto: friendReq) => undoDeletedFriend(dto),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["deletedList", memberId],
      });
    },
  });

  const handleUndeleteFriendRequest = (receiverId: number) => {
    if (!memberId || !receiverId) {
      alert("요청자 또는 친구 정보가 올바르지 않습니다.");
      return;
    }

    const dto: friendReq = {
      requesterId: memberId,
      receiverId: receiverId,
    };

    // useMutation의 mutate 함수 호출
    undoDeletedFriendMutation.mutate(dto);
  };

  return (
    <div id="app-container" className="min-h-screen p-6 pb-20">
      {/* 상단 바 */}
      <header className="relative flex items-center mb-6">
        <button className="absolute left-0 p-1" onClick={() => navigate(-1)}>
          <img src={backIcon} alt="Back" className="w-8 h-8" />
        </button>
        <h1 className="text-3xl font-bold w-full text-center">
          삭제
        </h1>
      </header>

      {/* 차단된 친구 목록 */}
      <div className="flex-1 overflow-y-auto pb-20">
        <section className="mb-6">
          <div className="pb-2">
            {isLoading ? (
              <p className="text-sm text-gray-400">로딩 중...</p>
            ) : deletedFriends.length === 0 ? (
              <p className="text-sm text-[#757575]">삭제한 친구가 없습니다.</p>
            ) : (
              <ul className="space-y-4">
                {deletedFriends.map((friend) => (
                  <li
                    key={friend.friendId}
                    className="relative flex items-center justify-between"
                  >
                    <span className="font-medium">
                      {friend.friendName}
                    </span>
                    <button
                      onClick={() =>
                        handleUndeleteFriendRequest(friend.friendId)
                      }
                      className="px-3 py-2 bg-red-500 text-white rounded-md text-sm hover:bg-red-600 transition-colors"
                    >
                      삭제 해제
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

export default DeleteList;
