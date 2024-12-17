import { tacoCoreApi } from "./tacoApis";
import { FavoriteAccountReq } from "../types/accountTypes";
import { TransactionReq } from "../types/transactionTypes";
import { MainAccount } from "../types/homeTypes";

// 사용자별 계좌 조회
export const getAccountList = async (memberId: number)=> {
  const response = await tacoCoreApi.get(`/core/accounts/list/${memberId}`);
  // console.log(response)
  return response.data;
};

// 홈 화면 사용자 정보 조회
export const connectAccount = async (memberId: number) => {
  const response = await tacoCoreApi.post("/core/accounts", memberId);
  return response.data;
};

export const getTransferList = async (dto: TransactionReq) => {
  console.log(dto)
  const response = await tacoCoreApi.post(`/core/transactions/list`, dto);
  console.log(response.data)
  return response.data; 
};

// 메인 계좌 선택
export const mainAccountSelect = async (dto: MainAccount
) => {
  const response = await tacoCoreApi.post("/core/accounts/set-main", dto);
  return response.data;
};

// 메인 계좌 수정
export const mainAccountEdit = async (dto: MainAccount) => {
  const response = await tacoCoreApi.put("/core/accounts/update-main", dto);
  return response.data;
};

// 즐겨찾기 계좌 선택
export const favoriteAccountSelect = async (dto: FavoriteAccountReq) => {
  const response = await tacoCoreApi.post(
    "/core/accounts/favorite-account",
    dto
  );
  return response.data;
};

// 즐겨찾기 계좌 삭제
export const favoriteAccountDelete = async (dto: FavoriteAccountReq) => {
  const response = await tacoCoreApi.delete("/core/accounts/favorite-account", {
    data: dto,
  });
  return response.data;
};

// 즐겨찾기 계좌 조회
export const favoriteAccountList = async (memberId: number) => {
  const response = await tacoCoreApi.get(
    `/core/accounts/favorite-account/list/${memberId}`
  );
  return response.data;
};
