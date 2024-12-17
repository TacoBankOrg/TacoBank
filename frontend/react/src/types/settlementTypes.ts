// 개별 계좌 정보 타입
export interface Account {
  accountHolder: string; // 예금주 이름
  accountNum: string;    // 계좌 번호
  bankName: string;      // 은행 이름
}

// 포함된 정산 정보 타입
export interface IncludedSettlement {
  settlementId: number;    // 정산 ID
  createdDate: string;     // 정산 생성 날짜
  memberAmount: number;    // 개별 정산 금액
  memberStatus: string;    // 개별 정산 상태
  leaderId: number;
  account: Account;        // 계좌 정보
}

// 생성된 정산 정보 타입
export interface CreatedSettlement {
  settlementId: number;    // 정산 ID
  groupId: number;         // 그룹 ID
  groupName: string;       // 그룹 이름
  totalAmount: number;     // 총 금액
  totalStatus: string;     // 정산 상태
  createdDate: string;     // 정산 생성 날짜
  completedDate: string;   // 정산 완료 날짜
}

// API 응답 타입
export interface SettlementResponse {
  createdSettlements: CreatedSettlement[];  // 생성된 정산 리스트
  includedSettlements: IncludedSettlement[]; // 포함된 정산 리스트
  availableAccounts: Account[];          // 사용 가능한 계좌 리스트
}


// 개별 정산 상세 정보 타입
export interface SettlementDetail {
  groupMemberId: number;     // 그룹 구성원 ID
  groupMemberName: string;   // 그룹 구성원 이름
  groupMemberAmount: number; // 그룹 구성원 금액
  groupMemberStatus: string; // 그룹 구성원 상태 (e.g., "N" - 진행중, "Y" - 완료)
  leaderId: number;
  updatedDate: string;       // 상태 업데이트 날짜
}

// 정산 상세 응답 데이터 타입
export interface SettlementDetailResponse {
  settlementId: number;            // 정산 ID
  memberId: number;
  settlementDetailsList: SettlementDetail[]; // 정산 상세 정보 리스트
}

// 정산요청 Reqeust DTO
export interface SettlementRequestDto {
  type: string; // 정산 유형 (e.g., "general" | "receipt")
  leaderId: number; // 리더 ID
  groupId: number | null; // 그룹 ID (optional)
  settlementAccountId: number; // 정산 계좌 ID
  totalAmount: number; // 총 금액
  friendIds: number[]; // 친구 ID 리스트
  memberAmounts: {
    memberId: number; // 멤버 ID
    amount: number; // 금액
  }[];
  receiptId?: number; // 영수증 ID (optional)
  productMemberDetails?: {
    productId: number; // 품목 ID
    productMembers: number[]; // 포함된 멤버 ID 리스트
  }[]; // 품목별 멤버 리스트 (optional)
}