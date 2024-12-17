import { useState, useEffect } from "react";
import PhoneInput from "./PhoneInput";
import { sendSMS, checkSMS } from "../api/memberApi";
import { handleApiError } from "../utils/apiHelpers";
import {
  SmsConfirmRequestDto,
  SmsVerificationRequestDto,
} from "../types/accountTypes";
import axios from "axios";
import useAuth from "../hooks/useAuth";

interface SMSVerificationModalProps {
  isOpen: boolean;
  onClose: () => void;
  onVerifySuccess: () => void;
  verificationId?: number | null;
  tel?: string;
  resendCooldown?: number;
  purpose: "join" | "pw" | "pin" | "mydata" | "tel";
}

function SMSVerificationModal({
  isOpen,
  onClose,
  onVerifySuccess,
  purpose,
  verificationId: defaultId = null, // 기본값 설정
  tel: defaultTel = "", // 기본값 설정
  resendCooldown: propResendCooldown = 180,
}: SMSVerificationModalProps) {
  const {member} = useAuth() ;
  const [tel, setTel] = useState(defaultTel);
  const [inputCode, setInputCode] = useState("");
  const [isVerified, setIsVerified] = useState(false);
  const [isTimerActive, setIsTimerActive] = useState(false);
  const [verificationId, setVerificationId] = useState<number | null>(
    defaultId
  );
  const [resendCooldown, setResendCooldown] = useState<number>(
    propResendCooldown !== undefined ? propResendCooldown : 180
  );

  useEffect(() => {
    if (propResendCooldown !== undefined) {
      setResendCooldown(propResendCooldown);
    }
  }, [propResendCooldown]);

  useEffect(() => {
    setTel(defaultTel || "010");
  }, [defaultTel]);

  useEffect(() => {
    if (defaultId !== null && defaultId !== verificationId) {
      setVerificationId(defaultId);
    }
  }, [defaultId]);

  useEffect(() => {
    let timer: ReturnType<typeof setTimeout>;
    if (isTimerActive && resendCooldown > 0) {
      timer = setTimeout(() => setResendCooldown((prev) => prev - 1), 1000);
    }
    return () => clearTimeout(timer);
  }, [resendCooldown, isTimerActive]);

  const handlePhoneChange = (_formattedValue: string, rawValue: string) => {
    setTel(rawValue);
  };

  const handleinputCodeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const code = e.target.value.replace(/[^0-9]/g, "");
    setInputCode(code);
  };

  const handleVerifyClick = async () => {
    try {
      if (!tel) {
        alert("전화번호를 입력해주세요.");
        return;
      }
      console.log("Purpose for SMS Verification:", purpose); 
      const dto: SmsVerificationRequestDto = {
        tel,
        type: purpose,
        ...(purpose !== "join" && { memberId: member?.memberId as number }),
      };
      const response = await sendSMS(dto);
      setVerificationId(response.verificationId);
      setResendCooldown(180);
      setIsTimerActive(true);
      alert("인증번호가 발송되었습니다.");
    } catch (error) {
      if (axios.isAxiosError(error)) {
        const errorMessage = handleApiError(error); // 에러 메시지 처리
        alert(errorMessage);
      } else {
        alert("인증번호 발송에 실패했습니다. 다시 시도해주세요.");
      }
    }
  };

  const handleVerifyConfirm = async () => {
    if (!verificationId) {
      alert("인증 요청이 올바르지 않습니다. 다시 시도해주세요.");
      return;
    }
    try {
      const dto: SmsConfirmRequestDto = {
        type: purpose,
        verificationId,
        tel,
        inputCode,
        ...(purpose !== "join" && { memberId: member?.memberId }),
      };
      const response = await checkSMS(dto);
      if (response.status === "SUCCESS") {
        alert("인증이 완료되었습니다.");
        setIsVerified(true);
        onVerifySuccess();
      }
    } catch (error) {
      console.error("문자 검증 오류:", error);
      alert("인증 실패: 올바르지 않거나 만료된 코드입니다.");
    }
  };

  const handleResendCode = async () => {
    if (resendCooldown > 0) return;

    try {
      if (!tel) {
        alert("전화번호를 입력해주세요.");
        return;
      }
      const dto: SmsVerificationRequestDto = {
        tel,
        type: purpose,
        ...(purpose !== "join" && { memberId: member?.memberId }),
      };
      const response = await sendSMS(dto);
      setVerificationId(response.verificationId);
      setResendCooldown(180);
      setIsTimerActive(true);
      alert("인증 코드가 재전송되었습니다.");
    } catch (error) {
      if (axios.isAxiosError(error)) {
        const errorMessage = handleApiError(error); // 에러 메시지 처리
        alert(errorMessage);
      } else {
        alert("인증번호 발송에 실패했습니다. 다시 시도해주세요.");
      }
    }
  };

  const isConfirmButtonEnabled = inputCode.length === 6;

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center">
      <div className="bg-white p-4 rounded-lg shadow-lg w-96">
        {!isVerified ? (
          <div>
            <h2 className="text-xl font-bold mb-6">
            {purpose === "mydata" || purpose === "pin" ? "본인 인증" : "인증 코드 입력"}
            </h2>
            {purpose !== "join" && (
            <div className="mb-5">
              <div className="mx-auto">
                <PhoneInput
                  value={
                    tel && /^\d{11}$/.test(tel)
                      ? tel.replace(/(\d{3})(\d{4})(\d{4})/, "$1-$2-$3")
                      : tel || ""
                  }
                  onChange={handlePhoneChange}
                  onVerify={handleVerifyClick}
                  isModalConfirmed={isVerified}
                  buttonPosition="side"
                />
              </div>
            </div>
    )}
            {/* 인증번호 입력 */}
            <div className="flex items-center mb-3">
              <div className="relative flex-1">
                <input
                  type="text"
                  value={inputCode}
                  onChange={handleinputCodeChange}
                  maxLength={6}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:border-[#536DFE] focus:ring-0"
                  placeholder="인증 코드를 입력하세요"
                />
                <span className="absolute right-4 top-1/2 transform -translate-y-1/2 text-gray-500 text-sm">
                  {resendCooldown > 0
                    ? `${Math.floor(resendCooldown / 60)}:${String(
                        resendCooldown % 60
                      ).padStart(2, "0")}`
                    : ""}
                </span>
              </div>
              <button
                onClick={handleResendCode}
                disabled={resendCooldown > 0}
                className={`ml-2 px-3 py-2 rounded-md ${
                  resendCooldown === 0
                    ? "bg-[#536DFE] hover:bg-[#485acf] text-white"
                    : "bg-gray-300 text-gray-500 cursor-not-allowed"
                }`}
              >
                재전송
              </button>
            </div>

            {/* 확인 버튼 */}
            <div className="flex justify-end space-x-2">
              <button
                onClick={onClose}
                className="px-3 py-2 bg-gray-300 text-gray-700 rounded-md hover:bg-gray-400"
              >
                닫기
              </button>
              <button
                onClick={handleVerifyConfirm}
                disabled={!isConfirmButtonEnabled}
                className={`px-3 py-2 rounded-md ${
                  isConfirmButtonEnabled
                    ? "bg-[#536DFE] text-white hover:bg-[#485acf]"
                    : "bg-gray-300 text-gray-500 cursor-not-allowed"
                }`}
              >
                확인
              </button>
            </div>
          </div>
        ) : (
          <div>
            <h2 className="text-xl font-bold mb-6">인증 완료</h2>
            <p>인증이 성공적으로 완료되었습니다.</p>
            <div className="flex justify-end">
              <button
                onClick={onClose}
                className="px-3 py-2 bg-gray-300 text-gray-700 rounded-md"
              >
                닫기
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default SMSVerificationModal;