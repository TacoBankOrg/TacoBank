export interface Friends {
  friendId: number; // 친구 ID
  friendName: string; // 친구 이름
  liked: string; // 좋아요 여부
}

export interface friendReq {
  requesterId: number;
  receiverId: number;
}

export interface searchResult {
  memberId: number; // 멤버 ID
  name: string; // 이름
  email: string; // 이메일
  status: string
}

export interface listEntry {
  friendId: number; // 친구 ID
  friendName: string; // 친구 이름
}