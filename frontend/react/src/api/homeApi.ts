import { tacoCoreApi } from "./tacoApis";
import { PinReq, Transaction } from "../types/homeTypes";

//홈화면 정보 요청
export const requestHome = async () => {
  const response = await tacoCoreApi.get("/core/home");
  return response.data;
};


//거래내역 조회
export const requestHomeTran = async (dto: Transaction) => {
  const response = await tacoCoreApi.post("/core/home/transactions/list", dto);
  return response.data;
};

//PIN 생성요청
export const createPIN = async (dto: PinReq) => {
    const response = await tacoCoreApi.post("/core/members/pin", dto);
    return response.data;
};