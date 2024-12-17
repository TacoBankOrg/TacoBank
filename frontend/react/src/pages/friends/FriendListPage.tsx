import React, { useState, useMemo, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import backIcon from "../../assets/image/icon/backicon.png";
import BottomNav from "../../components/BottomNav";
import {
  acceptFriend,
  blockFriend,
  deleteFriend,
  likeFriend,
  recvList,
  rejectFriend,
  unlikeFriend,
} from "../../api/friendApi";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import StarIcon from "../../assets/image/icon/starIcon";
import { Friends, friendReq, listEntry } from "../../types/friendTypes";
import { useFriendList } from "../../hooks/useFriendList";
import useAuth from "../../hooks/useAuth";

type FriendItemProps = {
  friend: Friends;
  toggleStar: (id: number) => void;
  setActionType: React.Dispatch<React.SetStateAction<"삭제" | "차단" | null>>;
  setSelectedFriend: React.Dispatch<React.SetStateAction<Friends | null>>;
  setShowConfirmModal: React.Dispatch<React.SetStateAction<boolean>>;
};

const FriendItem: React.FC<FriendItemProps> = ({
  friend,
  toggleStar,
  setActionType,
  setSelectedFriend,
  setShowConfirmModal,
}) => {
  return (
    <div className="relative w-full overflow-hidden">
      {/* 카드 본체 */}
      <div className="flex items-center justify-between p-4 rounded-md">
        {/* 이름과 별 */}
        <div className="flex items-center space-x-2">
          <span className="font-medium">{friend.friendName}</span>
          <button
            onClick={(e) => {
              e.stopPropagation(); // 스와이프와 별 클릭 이벤트 분리
              toggleStar(friend.friendId);
            }}
          >
            <StarIcon filled={friend.liked === "Y"} size={24} />
          </button>
        </div>

        {/* 차단/삭제 버튼 */}
        <div className="flex space-x-2">
          <button
           className="px-3 py-1 bg-[#757575] text-white rounded-md text-sm hover:bg-[#424242]"
            onClick={() => {
              setActionType("삭제");
              setSelectedFriend(friend);
              setShowConfirmModal(true);
            }}
          >
            삭제
          </button>
          <button
            className="px-3 py-1 bg-red-500 text-white rounded-md text-sm hover:bg-red-600"
            onClick={() => {
              setActionType("차단");
              setSelectedFriend(friend);
              setShowConfirmModal(true);
            }}
          >
            차단
          </button>
        </div>
      </div>
    </div>
  );
};

type ReceivedRequestItemProps = {
  request: listEntry;
  handleAcceptFriendRequest: (id: number) => void;
  handleRejectFriendRequest: (id: number) => void;
};

const ReceivedRequestItem: React.FC<ReceivedRequestItemProps> = ({
  request,
  handleAcceptFriendRequest,
  handleRejectFriendRequest,
}) => {
  return (
    <div key={request.friendId} className="flex items-center justify-between p-4">
      <span className="font-medium text-gray-800">{request.friendName}</span>
      <div className="flex gap-2">
        <button
          className="px-3 py-1 bg-[#536DFE] text-white rounded-md text-sm hover:bg-[#485acf]"
          onClick={() => handleAcceptFriendRequest(request.friendId)}
        >
          수락
        </button>
        <button
          className="px-3 py-1 bg-red-500 text-white rounded-md text-sm hover:bg-red-600"
          onClick={() => handleRejectFriendRequest(request.friendId)}
        >
          거절
        </button>
      </div>
    </div>
  );
};

const FriendList: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [actionType, setActionType] = useState<"삭제" | "차단" | null>(null);
  const [selectedFriend, setSelectedFriend] = useState<Friends | null>(null);
  const optionsRef = useRef<HTMLDivElement>(null);
  const [showOptions, setShowOptions] = useState(false);
  const toggleOptions = () => setShowOptions((prev) => !prev);
  const { authData } = useAuth();
  const memberId = authData?.memberId

  
  const { data: friends = [], isLoading: isFriendsLoading } = useFriendList(memberId as number);
  const { data: receivedRequests = [], isLoading: isRecvLoading } = useQuery<listEntry[], Error>({
    queryKey: ["recvList", memberId],
    queryFn: async (): Promise<listEntry[]> => {
      const response = await recvList(memberId as number);
      if (!response.data || response.status !== "SUCCESS") {
        throw new Error(response.message || "받은 신청 친구 목록 조회 실패");
      }
      return response.data as listEntry[];
    },
  });

  const handleAcceptFriendRequest = (receiverId: number) => {
    if (!memberId || !receiverId) {
      alert("요청자 또는 친구 정보가 올바르지 않습니다.");
      return;
    }

    const dto: friendReq = {
      requesterId: memberId,
      receiverId: receiverId,
    };

    // useMutation의 mutate 함수 호출
    acceptFriendMutation.mutate(dto);
  };

  // 친구 삭제
  const deleteFriendMutation = useMutation({
    mutationFn: (friendId: number) => deleteFriend(memberId as number, friendId),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["friendList", memberId],
      });
    },
  });

  // 친구 차단
  const blockFriendMutation = useMutation({
    mutationFn: (friendId: number) => blockFriend(memberId as number, friendId),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["friendList", memberId],
      });
    },
  });

  const acceptFriendMutation = useMutation({
    mutationFn: async (dto: friendReq) => {
      await acceptFriend(dto);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["recvList", memberId],
        refetchType: "active",
      });
      queryClient.invalidateQueries({
        queryKey: ["friendList", memberId],
        refetchType: "active",
      });
      // alert("친구 요청이 성공적으로 수락되었습니다.");
    },
    onError: (error) => {
      console.error("API 요청 실패", error);
    },
  });

  const handleRejectFriendRequest = (receiverId: number) => {
    if (!memberId || !receiverId) {
      alert("요청자 또는 친구 정보가 올바르지 않습니다.");
      return;
    }

    const dto: friendReq = {
      requesterId: memberId,
      receiverId: receiverId,
    };

    // useMutation의 mutate 함수 호출
    rejectFriendMutation.mutate(dto);
  };

  const rejectFriendMutation = useMutation({
    mutationFn: (dto: friendReq) => rejectFriend(dto),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["recvList", memberId],
      });
    },
  });

  const sortedFriends = useMemo(() => {
    return [...friends].sort(
      (a, b) => (b.liked === "Y" ? 1 : 0) - (a.liked === "Y" ? 1 : 0)
    );
  }, [friends]);

  const handleConfirmAction = () => {
    if (actionType && selectedFriend) {
      if (actionType === "삭제") {
        deleteFriendMutation.mutate(selectedFriend.friendId);
      } else if (actionType === "차단") {
        blockFriendMutation.mutate(selectedFriend.friendId);
      }
      setShowConfirmModal(false);
      setActionType(null);
      setSelectedFriend(null);
    }
  };

  const toggleStar = async (friendId: number) => {
    const friend = friends.find((f) => f.friendId === friendId);
    if (!friend) return;

    const isCurrentlyLiked = friend.liked === "Y"; // 현재 좋아요 상태 확인
    const newLikedStatus = isCurrentlyLiked ? "N" : "Y"; // 상태 반전

    // UI 즉시 업데이트
    queryClient.setQueryData(
      ["friendList", memberId],
      (oldFriends: Friends[] | undefined) => {
        if (!oldFriends) return;
        return oldFriends.map((f) =>
          f.friendId === friendId ? { ...f, liked: newLikedStatus } : f
        );
      }
    );

    try {
      const dto: friendReq = {
        requesterId: memberId as number,
        receiverId: friendId,
      };
      if (isCurrentlyLiked) {
        // 좋아요 취소 요청
        await unlikeFriend(dto);
        console.log("좋아요 취소가 서버에 업데이트되었습니다.");
      } else {
        // 좋아요 요청
        await likeFriend(dto);
        console.log("좋아요가 서버에 업데이트되었습니다.");
      }
    } catch (error) {
      console.error("좋아요/좋아요 취소 실패:", error);

      // 실패 시 UI를 원래 상태로 복원
      queryClient.setQueryData(
        ["friendList", memberId],
        (oldFriends: Friends[] | undefined) => {
          if (!oldFriends) return;
          return oldFriends.map((f) =>
            f.friendId === friendId ? { ...f, liked: friend.liked } : f
          );
        }
      );
    }
  };
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent | TouchEvent) => {
      const target = event.target as HTMLElement;
      // 옵션 또는 버튼이 클릭된 경우 스와이프를 닫지 않음
      if (
        target.closest("button") || // 클릭한 요소가 버튼인지 확인
        (optionsRef.current && optionsRef.current.contains(target)) // 클릭한 요소가 옵션 메뉴 내부인지 확인
      ) {
        return;
      }
      setShowOptions(false);
    };

    document.addEventListener("mousedown", handleClickOutside);
    document.addEventListener("touchstart", handleClickOutside);

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
      document.removeEventListener("touchstart", handleClickOutside);
    };
  }, []);

  return (
    <div id="app-container" className="min-h-screen p-6 pb-20">
      <header className="relative flex items-center justify-between mb-6">
        <button className=" p-1" onClick={() => navigate("/")}>
          <img src={backIcon} alt="Back" className="w-8 h-8" />
        </button>
        <h1 className="text-3xl font-bold mx-auto text-center">
          친구
        </h1>
        <div className="p-1" ref={optionsRef}>
          <button onClick={toggleOptions}>
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width={24}
              height={24}
              fill="currentColor"
              className="text-gray-600"
              viewBox="0 0 24 24"
            >
              <path d="M12 7a2 2 0 100-4 2 2 0 000 4zm0 6a2 2 0 100-4 2 2 0 000 4zm0 6a2 2 0 100-4 2 2 0 000 4z" />
            </svg>
          </button>
          {showOptions && (
            <div className="absolute top-0 right-0 w-24 bg-white border border-gray-200 rounded-md z-10">
              <button
                className="w-full py-2 px-3 text-center"
                onClick={() => {
                  navigate("/addfriend");
                  setShowOptions(false);
                }}
              >
                친구 추가
              </button>
              <div className="border-t border-[#E6E6E6]]"></div>
              <button
                className="w-full py-2 px-3 text-center"
                onClick={() => {
                  navigate("/deletedlist");
                  setShowOptions(false);
                }}
              >
                삭제 목록
              </button>
              <div className="border-t border-[#E6E6E6]"></div>
              <button
                className="w-full text-red-500 py-2 px-3 text-center"
                onClick={() => {
                  navigate("/blocklist");
                  setShowOptions(false);
                }}
              >
                차단 목록
              </button>
            </div>
          )}
        </div>
      </header>
      <div className="flex-1 overflow-y-auto pb-20">
        <section className="mb-6">
          <h2 className="text-m font-semibold mb-2">받은 신청</h2>
          {isRecvLoading ? (
            <p>로딩 중...</p>
          ) : receivedRequests.length === 0 ? (
            <p className="text-sm text-[#757575]">받은 신청이 없습니다.</p>
          ) : (
            receivedRequests.map((request) => (
              <ReceivedRequestItem
                key={request.friendId}
                request={request}
                handleAcceptFriendRequest={handleAcceptFriendRequest}
                handleRejectFriendRequest={handleRejectFriendRequest}
              />
            ))
          )}
        </section>

        <section>
          <h2 className="text-m font-semibold mb-2">친구</h2>
          {isFriendsLoading ? (
            <p>로딩 중...</p>
          ) : sortedFriends.length === 0 ? (
            <p className="text-sm text-[#757575]">친구가 없습니다.</p>
          ) : (
            sortedFriends.map((friend) => (
              <FriendItem
                key={friend.friendId}
                friend={friend}
                toggleStar={toggleStar}
                setActionType={setActionType}
                setSelectedFriend={setSelectedFriend}
                setShowConfirmModal={setShowConfirmModal}
              />
            ))
          )}
        </section>
      </div>

      {showConfirmModal && selectedFriend && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-20">
          <div className="bg-white rounded-lg p-6 w-80">
            <p className="font-medium text-lg">
              {selectedFriend.friendName}님을 {actionType}하시겠습니까?
            </p>
            <div className="flex justify-end mt-4 space-x-4">
              <button
                className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300"
                onClick={() => setShowConfirmModal(false)}
              >
                취소
              </button>
              <button
                className="px-4 py-2 bg-[#536DFE] text-white rounded hover:bg-[#485acf]"
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

export default FriendList;