import { tacoCoreApi } from "./tacoApis";
import { ChangePasswordReq, ChangePinReq, FindPwCheckReqDto, FindPwSmsReqDto, SetPinRequestDto, UpdateMemberInfoReq, validatePin } from "../types/memberTypes";

import { SmsConfirmRequestDto, SmsVerificationRequestDto } from "../types/accountTypes";
import { SmsVerificationRsp } from "../types/homeTypes";
import { ApiResponse } from "../types/commonTypes";
import { handleApiResponse } from "../utils/apiHelpers";

export const sendSMS = async (dto: SmsVerificationRequestDto): Promise<SmsVerificationRsp> => {
  console.log("Sending SMS DTO:", dto);
  const response = await tacoCoreApi.post<ApiResponse<SmsVerificationRsp>>("/core/auth/sms/verification", dto);
  console.log(response.data)
  return handleApiResponse(response.data);
};

export const checkSMS = async (dto: SmsConfirmRequestDto) => {
  const response = await tacoCoreApi.put("/core/auth/sms/verification", dto);
  return response.data;
};

export const findEmail = async (tel: string) => {
  const response = await tacoCoreApi.post("/core/members/email-recovery", {tel})
  console.log(response)
  return response.data
}

export const findPassword = async(dto: FindPwSmsReqDto) => {
  const response = await tacoCoreApi.post("/core/members/password-recovery/verify", dto)
  console.log(response)
  return response.data.data
}

export const findPasswordCheck = async(dto: FindPwCheckReqDto) => {
  const response = await tacoCoreApi.post("/core/members/password-recovery/confirm", dto)
  console.log(response)
  return response.data
}

// 알림 조회
export const getNotifications = async (memberId: number) => {
  const response = await tacoCoreApi.get(`/core/notifications/${memberId}`);
  console.log(response.data)
  return response.data.data
}

// 알림 읽음 여부
export const readNotifications = async (notificationId: number) => {
  const response = await tacoCoreApi.patch(`/core/notifications/${notificationId}`)
  console.log(response.data)
  return response.data
}

// 회원정보 조회
export const getMember = async (memberId: number) => {
  const response = await tacoCoreApi.get(`/core/members/${memberId}`);
  return response.data
}

// 회원정보수정
export const editMember = async (dto: UpdateMemberInfoReq, memberId: number) => {
  const response = await tacoCoreApi.put(`/core/members/${memberId}`, dto)
  return response.data;
}

//비밀번호 변경
export const changePassword = async (dto: ChangePasswordReq) => {
  const response = await tacoCoreApi.put(`/core/members/password`, dto);
  return response.data
}

//현재 PIN 검증
export const currentPin = async (dto: validatePin) => {
  const response = await tacoCoreApi.post(`/core/members/pin-validate`, dto);
  return response.data
}

//PIN 재설정(입력호출)
export const resetPin = async (dto: SetPinRequestDto) => {
  const response = await tacoCoreApi.post(`/core/members/pin`, dto);
  return response.data
}

//PIN 변경
export const changePin = async (dto: ChangePinReq) => {
  const response = await tacoCoreApi.post(`/core/members/pin-reset`, dto);
  return response.data
}

//회원탈퇴
export const deactivation = async (memberId: string) => {
  const response = await tacoCoreApi.put(`/core/members/${memberId}/deactivation`);
  return response.data
}

//로그아웃
export const logout = async (dto: ChangePasswordReq) => {
  const response = await tacoCoreApi.put(`/core/members/password`, dto);
  return response.data
}