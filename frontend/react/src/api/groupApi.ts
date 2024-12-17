import { tacoCoreApi } from "./tacoApis";

// 나의 그룹 조회
export const groupList = async (memberId: number) => {
  try {
    console.log(`Fetching group list for memberId: ${memberId}`); // 로그 추가
    const response = await tacoCoreApi.get(
        `/core/groups/my-group/list/${memberId}`
    );
    console.log("API response:", response.data.data); // 응답 확인
    
    // 응답 맞춰야할듯
    return response.data;

  } catch (error) {
    console.error("Error fetching group list:", error);
    return [];
  }
};

// 내가 리더인 그룹 조회
export const leaderGroupList = async (memberId: number) => {
  try {
    console.log(`Fetching group list for memberId: ${memberId}`); // 로그 추가
    const response = await tacoCoreApi.get(
        `/core/groups/leader-group/list/${memberId}`
    );
    console.log("API response:", response.data.data); // 응답 확인

    // 응답 맞춰야할듯
    return response.data;

  } catch (error) {
    console.error("Error fetching group list:", error);
    return [];
  }
};

// 그룹 초대 받은 목록 조회
export const recvList = async (memberId: number) => {
  try {
  const response = await tacoCoreApi.get(`/core/groups/invitations/${memberId}`);
  return response.data;
  } catch (error) {
    console.error("Error fetching group list:", error);
    return []
  }
};

// 그룹 생성
export const createGroup = async (memberId: number, groupName: string) => {
  const response = await tacoCoreApi.post("/core/groups", {
    leaderId: memberId,
    groupName,
  });
  return response.data.data;
};

// 그룹 비활성화
export const deleteGroup = async (memberId: number, groupId: number) => {
  const response = await tacoCoreApi.post("/core/groups/deactivate", { memberId, groupId });
  return response.data;
};

// 그룹 초대
export const inviteGroup = async (
  memberId: number,
  groupId: number,
  friendId: number
) => {
  const response = await tacoCoreApi.post("/core/groups/invite", {
    memberId,
    groupId,
    friendId,
  });
  return response.data; // 성공 응답 데이터 반환
};

// 그룹 초대 가능한 친구 조회
export const inviteFriends = async (memberId: number) => {
  const response = await tacoCoreApi.get(`/core/groups/${memberId}/friends/list`);
  return response.data;
};

// 그룹 나가기
export const leaveGroup = async (memberId: number, groupId: number) => {
  const response = await tacoCoreApi.post("/core/groups/leave", { memberId, groupId });
  return response.data;
};

// 그룹 멤버 추방
export const expelGroupMember = async (
  memberId: number,
  groupId: number,
  targetMemberId: number
) => {
  const response = await tacoCoreApi.post("/core/groups/expel", {
    memberId,
    groupId,
    targetMemberId,
  });
  return response.data; // 성공 응답 데이터 반환
};

// 그룹 수락
export const acceptGroup = async (memberId: number, groupId: number) => {
  const response = await tacoCoreApi.post("/core/groups/accept", {
    memberId,
    groupId,
  });
  return response.data;
};

// 그룹 거절
export const rejectGroup = async (memberId: number, groupId: number) => {
  const response = await tacoCoreApi.post("/core/groups/reject", {
    memberId,
    groupId,
  });
  return response.data;
};

// 그룹 검색
export const searchGroup = async (groupName: string) => {
  const response = await tacoCoreApi.get(`/core/groups/search/${groupName}`);
  return response.data;
};
