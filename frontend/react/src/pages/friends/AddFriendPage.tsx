import React, { useState, useRef } from "react";
import { useNavigate } from "react-router-dom";
import backIcon from "../../assets/image/icon/backicon.png";
import { searchFriend, sendFriend } from "../../api/friendApi";
import { friendReq, searchResult } from "../../types/friendTypes";
import axios from "axios";
import { handleApiError } from "../../utils/apiHelpers";
import useAuth from "../../hooks/useAuth";
import BottomNav from "../../components/BottomNav";

const AddFriendPage: React.FC = () => {
  const navigate = useNavigate();
  const inputRef = useRef<HTMLInputElement>(null); // 입력 필드 참조
  const [searchResult, setSearchResult] = useState<searchResult | null>(null);
  const [noResults, setNoResults] = useState(false); // 검색 결과 없음 상태
  const [isLoading, setIsLoading] = useState(false); // 로딩 상태
  const [errorMessage, setErrorMessage] = useState<string | null>(null); 
  const { authData } = useAuth();
  const memberId = authData?.memberId

  // 검색 실행 함수
  const handleSearch = async () => {
    const email = inputRef.current?.value?.trim(); // 입력값 가져오기 및 트림
    if (!email) {
      alert("이메일을 입력해주세요.");
      setSearchResult(null);
      setNoResults(false);
      return;
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      alert("올바른 이메일 형식으로 입력하세요."); // 이메일 형식 오류 메시지
      setSearchResult(null);
      setNoResults(false);
      return;
    }

    setIsLoading(true);
    try {
      const response = await searchFriend(email, memberId as number); // API 호출
      console.log("API 응답:", response);
      if (response) {
        setSearchResult(response); // 검색 결과 업데이트
        setNoResults(false);
      } else {
        setSearchResult(null);
        setNoResults(true);
      }
    } catch (error) {
      console.error("회원 검색 중 오류:", error);
      setSearchResult(null);
      setNoResults(true);
      // 오류 처리
      if (axios.isAxiosError(error)) {
        const errorMessage = handleApiError(error); // 400 에러 메시지 처리
        setErrorMessage(errorMessage);
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleFriendRequest = async () => {
    if (!memberId || !searchResult?.memberId) {
      alert("요청자 또는 친구 정보가 올바르지 않습니다.");
      return;
    }

    try {
      const dto: friendReq = {
        requesterId: memberId,
        receiverId: searchResult.memberId,
      };

      console.log(dto);
      await sendFriend(dto); // 친구 요청 API 호출

      alert("친구 요청이 성공적으로 처리되었습니다."); // 성공 메시지

      // 검색 결과 초기화
      inputRef.current!.value = ""; // 검색 입력 필드 초기화
      setSearchResult(null); // 검색 결과 상태 초기화
      setNoResults(false); // 검색 결과 없음 상태도 초기화
    } catch (error) {
      console.error("API 요청 실패", error);
    }
  };

  return (
    <div id="app-container" className="min-h-screen p-6 pb-20">
      {/* 상단 바 */}
      <div className="relative flex items-center justify-between mb-6">
        <button className="absolute left-0 p-1" onClick={() => navigate(-1)}>
          <img src={backIcon} alt="Back" className="w-8 h-8" />
        </button>
        <h1 className="text-3xl font-bold w-full text-center">친구 추가</h1>
      </div>

      {/* 검색 바 */}
      <div className="flex items-center bg-gray-100 rounded-full px-3 py-2 mb-4">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          className="w-5 h-5 text-gray-400"
          viewBox="0 0 24 24"
          fill="none"
        >
          <path
            d="M10 18a8 8 0 100-16 8 8 0 000 16zM21 21l-4.35-4.35"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        </svg>
        <input
          ref={inputRef} // input 요소를 참조
          type="text"
          placeholder="이메일 검색"
          onKeyPress={(e) => e.key === "Enter" && handleSearch()} // Enter로 검색
          onChange={() => {
            setSearchResult(null); // 검색 결과 초기화
            setNoResults(false); // 검색 결과 없음 상태 초기화
            setErrorMessage(null); // 에러 메시지 초기화
          }}
          className="w-full bg-transparent pl-3 text-sm text-gray-600 focus:outline-none placeholder-gray-400"
        />
        <button
          onClick={handleSearch}
          className="p-1 ml-2 text-xs"
          style={{ whiteSpace: "nowrap" }}
        >
          확인
        </button>
      </div>

      {/* 검색 결과 */}
      {isLoading ? (
        <p className="text-gray-500 text-sm mt-4">검색 중...</p>
      ) : searchResult ? (
        <div className="flex items-center justify-between p-4">
          {/* 이름만 표시 */}
          <span className="text-gray-800 font-medium">{searchResult.name}</span>
          {searchResult.status === "ACC" ? (
            <button
              disabled
              className="px-2 py-1 bg-gray-300 text-gray-500 rounded-md text-sm cursor-not-allowed"
            >
              이미 친구
            </button>
          ) : searchResult.status === "REQ" ? (
            <button
              disabled
              className="px-2 py-1 bg-gray-300 text-gray-500 rounded-md text-sm cursor-not-allowed"
            >
              이미 요청
            </button>
          ) : searchResult.status === "REQ_RECEIVED" ? (
            <button
              disabled
              className="px-2 py-1 bg-gray-300 text-gray-500 rounded-md text-sm cursor-not-allowed"
            >
              요청받음
            </button>
          ) : (
            <button
              onClick={handleFriendRequest}
              className="px-2 py-1 bg-[#536DFE] text-white rounded-md text-sm hover:bg-[#485acf] transition-colors"
            >
              친구 요청
            </button>
          )}
        </div>
      ) : noResults ? (
        <p className="text-gray-500 text-sm mt-4">{errorMessage}</p>
      ) : null}
      <footer>
        <BottomNav />
      </footer>
    </div>
    
  );
};

export default AddFriendPage;
