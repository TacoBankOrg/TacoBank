import { tacoCoreApi } from "./tacoApis";
import { friendReq, searchResult } from "../types/friendTypes";
import { ApiResponse } from "../types/commonTypes";
import { handleApiResponse } from "../utils/apiHelpers";


// 친구 요청
export const sendFriend = async (dto: friendReq) => {
  const response = await tacoCoreApi.post("/core/friends/request", dto);
  return handleApiResponse(response.data);
};

// 친구 수락
export const acceptFriend = async (dto: friendReq) => {
  const response = await tacoCoreApi.post("/core/friends/accept", dto);
  return handleApiResponse(response.data);
};

// 친구 요청 거절
export const rejectFriend = async (dto: friendReq) => {
  const response = await tacoCoreApi.post("/core/friends/reject", dto);
  return handleApiResponse(response.data);
};

// 친구 차단
export const blockFriend = async (memberId: number, receiverId: number) => {
  const response = await tacoCoreApi.post("/core/friends/block", {
    requesterId: memberId,
    receiverId,
  });
  return response.data;
};

// 차단 해제
export const unblockFriend = async (dto: friendReq) => {
  const response = await tacoCoreApi.post("/core/friends/unblock", dto);
  return handleApiResponse(response.data);
};

// 친구 삭제
export const deleteFriend = async (memberId: number, receiverId: number) => {
  const response = await tacoCoreApi.post("/core/friends", {
    requesterId: memberId,
    receiverId,
  });
  return response.data;
};

// 차단된 친구 목록 조회
export const blockedList = async (memberId: number) => {
  const response = await tacoCoreApi.get(`/core/friends/blocked/${memberId}`);
  return response.data;
};

// 친구 요청 받은 목록 조회
export const recvList = async (memberId: number) => {
  const response = await tacoCoreApi.get(`/core/friends/received/list/${memberId}`);
  console.log(response.data)
  return response.data;
};

// 친구 목록 조회
export const friendList = async (memberId: number) => {
  const response = await tacoCoreApi.get(`/core/friends/list/${memberId}`);
  return response.data;
};

// 삭제 목록 조회
export const deleteFriendList = async (memberId: number) => {
  const response = await tacoCoreApi.get(`/core/friends/deleted/list/${memberId}`);
  console.log(response)
  return response.data;
};


export const undoDeletedFriend = async (dto: friendReq) => {
  const response = await tacoCoreApi.post("/core/friends/undo-delete", dto);
  return handleApiResponse(response.data);
};

// 친구 검색
export const searchFriend = async (
    email: string,
    memberId: number
): Promise<searchResult> => {
  const response = await tacoCoreApi.get<ApiResponse<searchResult>>(
      `/core/members/search/${email}`, // email은 경로 변수로 전달
      {
        params: { memberId }, // memberId는 쿼리 파라미터로 전달
      }
  );

  // handleApiResponse로 응답 처리
  return handleApiResponse<searchResult>(response.data);
};



// 친구 좋아요
export const likeFriend = async (dto: friendReq) => {
  await tacoCoreApi.post("/core/friends/like", dto);
};

// 친구 좋아요 취소
export const unlikeFriend = async (dto: friendReq) => {
  await tacoCoreApi.post("/core/friends/unlike", dto);
};