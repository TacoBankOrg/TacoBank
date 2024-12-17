import React, { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import backIcon from "../../assets/image/icon/backicon.png";
import { inviteFriends, createGroup, inviteGroup } from "../../api/groupApi";
import useAuth from "../../hooks/useAuth.ts"; // API 호출 함수
import { inviteFriend } from "../../types/groupTypes.ts";
import { useQuery } from "@tanstack/react-query";
import BottomNav from "../../components/BottomNav.tsx";

const CreateGroup: React.FC = () => {
  const navigate = useNavigate();
  const [selectedFriends, setSelectedFriends] = useState<number[]>([]);
  const [groupName, setGroupName] = useState("");
  const [searchTerm, setSearchTerm] = useState("");
  const groupNameRef = useRef<HTMLInputElement>(null);
  const { authData } = useAuth();
  const memberId = authData?.memberId

  useEffect(() => {
    // 페이지 로드 시 그룹명 입력 필드에 포커싱
    if (groupNameRef.current) {
      groupNameRef.current.focus();
    }
  }, []);


  const {
    data: friends = [],
    isLoading,
  } = useQuery<inviteFriend[], Error>({
    queryKey: ["inviteList", memberId],
    queryFn: async (): Promise<inviteFriend[]> => {
      const response = await inviteFriends(memberId as number);
      if (!response.data || response.status !== "SUCCESS") {
        throw new Error(response.message || "초대 가능한 친구 목록 조회 실패");
      }
      return response.data as inviteFriend[];
    },
  });

  const toggleSelection = (id: number) => {
    setSelectedFriends((prevSelected) =>
      prevSelected.includes(id)
        ? prevSelected.filter((friendId) => friendId !== id) // 이미 선택된 경우 제거
        : [...prevSelected, id] // 선택되지 않은 경우 추가
    );
  };

  const filteredFriends = friends.filter((friend) =>
    friend.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const isCreateButtonEnabled =
    groupName.trim() !== "" && selectedFriends.length > 0;

  const handleCreateGroup = async () => {
    try {
      console.log("그룹 생성:", groupName);
      console.log("초대할 친구들:", selectedFriends);

      // 먼저 그룹을 생성합니다
      const groupResponse = await createGroup(memberId as number, groupName);
      const groupId = groupResponse.groupId; // 서버 응답에서 groupId를 가져옵니다
      console.log("생성된 그룹 ID:", groupId); // groupId가 잘 출력되는지 확인합니다

      // 이제 선택한 친구들을 초대합니다
      for (const friendId of selectedFriends) {
        console.log("친구 초대 중, 친구 ID:", friendId); // 친구 ID 로그 출력
        await inviteGroup(memberId as number, groupId, friendId);
      }

      // 성공 후에는 이동하거나 성공 메시지를 보여줍니다
      navigate("/groups");
    } catch (error) {
      console.error("그룹 생성 중 에러 발생:", error);
    }
  };

  return (
    <div id="app-container" className="min-h-screen p-6 pb-20">
      <header className="relative flex items-center mb-6">
        <button className="absolute left-0 p-1" onClick={() => navigate(-1)}>
          <img src={backIcon} alt="Back" className="w-8 h-8" />
        </button>
        <h1 className="text-3xl font-bold w-full text-center">
          그룹 만들기
        </h1>
      </header>

      {/* 그룹 이름 입력 */}
      <div className="flex flex-col mb-5">
        <input
          type="text"
          ref={groupNameRef} 
          value={groupName}
          onChange={(e) => setGroupName(e.target.value)}
          placeholder="그룹 이름을 입력해주세요 (필수)"
          className="p-2 border-b border-gray-300 focus:outline-none focus:border-gray-500"
        />
      </div>

      {/* 친구 검색 및 선택 */}
      <div className="flex flex-col rounded-md overflow-y-auto">
        <div className="flex items-center border-b border-gray-300 p-2 mb-2">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            className="h-5 w-5 text-gray-400"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              fillRule="evenodd"
              d="M12.293 13.707a8 8 0 111.414-1.414l4.829 4.829a1 1 0 01-1.414 1.414l-4.829-4.829zM8 14a6 6 0 100-12 6 6 0 000 12z"
              clipRule="evenodd"
            />
          </svg>
          <input
            type="text"
            placeholder="친구 검색"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="ml-2 w-full bg-transparent focus:outline-none"
          />
        </div>

        {/* 친구 리스트 로드 상태 처리 */}
        {isLoading && (
          <p className="text-gray-500 text-sm mt-2">친구 목록 로딩 중...</p>
        )}
        {!isLoading && filteredFriends.length === 0 && (
          <p className="text-[#757575] text-sm mt-2">검색 결과가 없습니다.</p>
        )}
        {!isLoading &&
          filteredFriends.map((friend) => (
            <div
              key={friend.friendId}
              className="p-2 rounded-md flex items-center cursor-pointer"
              onClick={() => toggleSelection(friend.friendId)}
            >
              <input
                type="radio"
                className="form-radio h-5 w-5 rounded-md"
                checked={selectedFriends.includes(friend.friendId)}
                onChange={() => toggleSelection(friend.friendId)} // radio 버튼 클릭 시에도 toggleSelection 작동
              />
              <span className="ml-3 text-gray-800">{friend.name}</span>
            </div>
          ))}
      </div>

      {/* 만들기 버튼 */}
      <button
        disabled={!isCreateButtonEnabled || isLoading} // 그룹 이름과 친구 선택이 완료되지 않으면 비활성화
        onClick={handleCreateGroup}
        className={`mt-4 w-full py-2 rounded-md ${
          isCreateButtonEnabled && !isLoading
            ? "bg-[#536DFE] text-white hover:bg-[#485acf]"
            : "bg-gray-300 text-gray-400 cursor-not-allowed"
        }`}
      >
        {isLoading ? "로딩 중..." : "만들기"}
      </button>
      <footer>
        <BottomNav />
      </footer>
    </div>
  );
};

export default CreateGroup;
