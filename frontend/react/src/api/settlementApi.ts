import { tacoCoreApi } from "./tacoApis";
import {Account} from "../types/accountTypes.ts";

// 사용자 계좌 조회
export const fetchUserAccounts = async (memberId: number): Promise<Account[]> => {
  const response = await tacoCoreApi.post("/core/accounts/list", { memberId });
  return response.data;
};


// 정산 현황
export const statusSettlement = async (memberId: number) => {
  const response = await tacoCoreApi.get(`/core/settlement/status/${memberId}`);
  console.log(response.data)
  return response.data;
};

// 정산 상세
export const detailSettlement = async (
  memberId: number,
  settlementId: number
) => {
  const response = await tacoCoreApi.get(
    `/core/settlement/${settlementId}/details/${memberId}`
  );
  return response.data;
};

// 독촉하기
export const notifySettlement = async (
  memberId: number,
  settlementId: number
) => {
  const response = await tacoCoreApi.post(
    `/core/settlement/${settlementId}/notify/${memberId}`
  );
  return response.data;
};

// 정산 요청
export const requestSettlement = async (data: {
  type: string;
  leaderId: number;
  groupId: number | null;
  settlementAccountId: number;
  totalAmount: number;
  friendIds: number[];
  memberAmounts: { memberId: number; amount: number }[];
}) => {
  //console.log(data)
  const response = await tacoCoreApi.post("/core/settlement", data);
  return response.data;
};
