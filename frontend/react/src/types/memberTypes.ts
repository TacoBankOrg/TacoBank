import { FavoriteAccount } from "./accountTypes";

export interface Member {
  memberId?: number;
  name?: string;
  birth?: string;
  gender?: string;
  email: string;
  tel?: string;
  password?: string;
  transferPin?: string;
  mainAccountId?: number;
  mydataLinked?: string
  FavoriteAccountList?: FavoriteAccount[];
}

export interface ChangePasswordReq {
  memberId: number;
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface validatePin{
  memberId: number
  transferPin: string
}

export interface ChangePinReq {
  memberId: number;
  currentPin: string;
  newPin: string;
  confirmPin: string;
}

export interface LogoutReq {
  memberId: number;
}

export interface MemberData {
  memberId: number;
  name: string;
  tel: string;
  birth: string;
  email: string;
}

export interface UpdateMemberInfoReq {
  name: string | undefined;
  tel: string;
}

export interface SetPinRequestDto {
  memberId: number;
  transferPin: string;
  confirmTransferPin: string
}
export interface FindPwSmsReqDto {
  email: string;
  tel: string;
}

export interface FindPwSmsRspDto {
  memberId: number;
  verificationId: number;
}

export interface FindPwSmsConfirmReqDto {
  type: string;
  tel: string;
  verificationId: number;
  inputCode: string;
}

export interface FindPwCheckReqDto{
  memberId: number;
  newPassword: string;
  confirmPassword: string;
}

export interface Notification {
  memberId: number;
  notificationId: number
  message: string;
  createdDate: string;
  is_read: boolean
};