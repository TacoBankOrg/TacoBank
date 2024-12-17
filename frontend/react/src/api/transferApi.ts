import { tacoCoreApi } from "./tacoApis";
import {
  BaroTransferReq,
  TransferReq,
  CheckReceiverReq,
} from "../types/transferTypes";

// 수취인조회
export const checkReceiver = async (dto: CheckReceiverReq) => {
  console.log(dto)
  const response = await tacoCoreApi.post(
    "/core/transfers/receiver",
    dto
  );
  console.log(response)
  return response.data.data;
};

// 출금 비번 검증
export const verifyPin = async (memberId: number, transferPin: string) =>{
  const response = await tacoCoreApi.post("/core/members/pin-validate", {memberId, transferPin})
  return response
}

// 바로송금 출금 계좌 잔액
export const baroTransfer = async (dto: BaroTransferReq) => {
  const response = await tacoCoreApi.post(
    "/core/settlement/transfers",
    dto
  );
  console.log(response)
  return response.data.data;
};

// 송금 출금 비번 검증
export const verifyTransferPin = async (dto: TransferReq) => {
  console.log(dto)
  const response = await tacoCoreApi.post(
    "/core/transfers/password/verify",
    dto
  );
  console.log(response.data)
  return response.data;
};

// 송금 요청
export const sendMoney = async (dto: TransferReq) => {
  console.log(dto)
  const response = await tacoCoreApi.post("/core/transfers", dto);
  console.log(response.data)
  return response.data;
};

// 즐찾, 최근, 친구 계좌 조회
export const getTransferOption = async (memberId: number) => {
  const response = await tacoCoreApi.get(
    `/core/accounts/transfer-options/${memberId}`
  );
  console.log(response.data)
  return response.data;
};