export interface Member {
  groupId?: number;
  memberId: number;
  memberName: string;
  status?: string; // 예: "ACCEPTED"
}

export interface Group {
  groupId: number; // 그룹 ID
  groupName: string; // 그룹 이름
  customized?: string; // 그룹 커스터마이징 여부 ("Y", "N")
  activated?: string; // 그룹 활성화 여부 ("Y", "N")
  leaderId: number; // 리더 ID
  leaderName: string; // 리더 이름
  members: Member[]; // 그룹 멤버 목록
}

export interface GroupListResponse {
  status: string; // 예: "success"
  message: string; // 예: "내가 속한 그룹 목록 조회 성공"
  data: Group[]; // 그룹 목록
}

export interface inviteFriend{
  friendId:number
  name: string
}

export interface GroupInvitation {
  groupId: number; // 초대받은 그룹 ID
  memberId: number; // 초대받은 멤버 ID
  status: string; // 초대 상태 (예: "INVITED")
  groupName: string; // 초대받은 그룹 이름
}

export interface SearchedGroup {
  groupId: number; // 검색된 그룹 ID
  groupName: string; // 검색된 그룹 이름
  members: Member[]; // 그룹 멤버 목록
}

export interface MyGroupsResponse {
  status: string; // API 응답 상태 (예: "success")
  message: string; // 응답 메시지 (예: "내가 속한 그룹 목록 조회 성공")
  data: Group[]; // 내가 속한 그룹 데이터
}

export interface GroupInvitationsResponse {
  status: string; // API 응답 상태 (예: "success")
  message: string; // 응답 메시지 (예: "초대 대기 목록 조회 성공")
  data: GroupInvitation[]; // 초대 대기 목록 데이터
}

export interface GroupSearchResponse {
  status: string; // API 응답 상태 (예: "success")
  message: string; // 응답 메시지 (예: "그룹 검색 성공")
  data: SearchedGroup; // 검색된 그룹 데이터
}

export interface GroupMember {
  memberId: number;
  memberName: string;
}

export interface GroupDetails {
  groupId: number;
  groupName: string;
  leaderId: number;
  leaderName: string
  members: GroupMember[];
}
