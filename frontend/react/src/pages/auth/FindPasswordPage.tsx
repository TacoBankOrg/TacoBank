import { useEffect, useRef, useState } from "react";
import backIcon from "../../assets/image/icon/backicon.png";
import { useNavigate } from "react-router-dom";
import PhoneInput from "../../components/PhoneInput";
import { checkSMS, findPassword } from "../../api/memberApi";
import {
  FindPwSmsConfirmReqDto,
  FindPwSmsReqDto,
  FindPwSmsRspDto,
} from "../../types/memberTypes";

export default function FindPassword() {
  const navigate = useNavigate();
  const [tel, setTel] = useState("010"); // 전화번호 상태 추가
  const [verificationState, setVerificationState] =
    useState<FindPwSmsRspDto | null>(null);
  const [showVerificationModal, setShowVerificationModal] = useState(false);
  const [resendCooldown, setResendCooldown] = useState(0);
  const [inputCode, setInputCode] = useState("");
  const [isVerified, setIsVerified] = useState(false);
  const phoneInputRef = useRef<HTMLInputElement>(null);
  const [email, setEmail] = useState(""); // 이메일 상태 추가
  const [emailError, setEmailError] = useState(false); 
  const [emailTouched, setEmailTouched] = useState(false);

  const handlePhoneChange = (_formattedValue: string, rawValue: string) => {
    setTel(rawValue);

    // 이메일이 올바르지 않은 상태에서 전화번호 입력 필드에 포커스가 갈 때 오류 표시
    if (!validateEmail(email)) {
      setEmailError(true);
    }
  };
  const handleEmailChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setEmail(e.target.value);

    // 이메일을 올바르게 입력하면 오류 메시지 숨김
    if (emailTouched && validateEmail(e.target.value)) {
      setEmailError(false);
    }
  };
  const handleinputCodeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const code = e.target.value.replace(/[^0-9]/g, "");
    setInputCode(code);
  };

  const validateEmail = (email: string) => {
    // 이메일 형식 유효성 검사
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  };

  const handleEmailKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Tab") {
      e.preventDefault(); // 기본 Tab 동작 막기

      if (!validateEmail(email)) {
        setEmailError(true); // 이메일이 유효하지 않으면 에러 메시지 표시
      } else {
        phoneInputRef.current?.focus(); // PhoneInput으로 포커스 이동
      }
    }
  };

  const handlePhoneInputFocus = () => {
    // 전화번호 필드를 터치했을 때 이메일 오류 상태 확인
    if (!validateEmail(email)) {
      setEmailTouched(true);
      setEmailError(true);
    }
  };

  useEffect(() => {
    let timer: ReturnType<typeof setTimeout>;
    if (resendCooldown > 0) {
      timer = setTimeout(() => setResendCooldown(resendCooldown - 1), 1000);
    }
    return () => clearTimeout(timer); // 타이머 정리
  }, [resendCooldown]);

  const handleSendSMS = async () => {
    try {
      const dto: FindPwSmsReqDto = {
        email,
        tel,
      };
      const response: FindPwSmsRspDto = await findPassword(dto);
      console.log("findPassword 응답:", response);
      setVerificationState(response);
      setShowVerificationModal(true);
      setResendCooldown(180);
    } catch (error) {
      console.error("API 요청 실패", error);
    }
  };

  const handleVerifyConfirm = async () => {
    if (!verificationState) {
      alert("인증 요청이 올바르지 않습니다. 다시 시도해주세요.");
      return;
    }
    try {
      const dto: FindPwSmsConfirmReqDto = {
        type: "pw",
        verificationId: verificationState.verificationId,
        tel,
        inputCode, // 인증번호 입력값
      };
      console.log(dto);
      const response = await checkSMS(dto);
      console.log(response);
      if (response.status === "SUCCESS") {
        alert("인증이 완료되었습니다.");
        setIsVerified(true); // 인증 성공 상태 업데이트
        navigate("/reset-password", {
          state: { memberId: verificationState.memberId },
        });
      }
    } catch (error) {
      console.error("API 요청 실패", error);
    }
  };

  const handleResendCode = async () => {
    if (resendCooldown > 0) return;
    await handleSendSMS(); // 인증 요청 재전송
  };
  const isConfirmButtonEnabled = inputCode.length === 6;
  return (
    <div id="app-container" className="flex min-h-screen p-6">
      <div className="relative flex items-center justify-center mb-6">
        <button
          className="absolute left-0 p-1"
          onClick={() => navigate("/login")}
        >
          <img src={backIcon} alt="Back" className="w-8 h-8" />
        </button>
        <h1 className="text-3xl font-bold mx-auto">
          비밀번호 찾기
        </h1>
      </div>

      {/* 탭 전환 버튼 */}
      <div className="flex justify-center mb-6 border-b border-gray-300">
        <button
          className="flex-1 py-2 text-center text-gray-500"
          onClick={() => navigate("/find-email")}
        >
          이메일 찾기
        </button>
        <button
          className="flex-1 py-2 text-center text-[#536DFE] border-b-2 border-[#536DFE] font-bold"
          onClick={() => navigate("/find-password")}
        >
          비밀번호 찾기
        </button>
      </div>

      {/* 이메일 및 전화번호 입력 필드와 찾기 버튼 */}
      <div className="space-y-4">
        <input
          type="text"
          placeholder="이메일"
          value={email}
          onChange={handleEmailChange}
          onKeyDown={handleEmailKeyDown}
          onBlur={() => setEmailTouched(true)} 
          className={`w-full px-3 py-2 border ${
            emailError ? "border-red-500" : "border-gray-300"
          } rounded-md focus:outline-none focus:border-[#536DFE] focus:ring-0`}
        />
        {emailError && emailTouched && (
          <p className="text-red-500 text-sm mt-1">올바른 이메일 형식을 입력해주세요.</p>
        )}
        <div className="mx-auto space-y-4">
          <PhoneInput
            ref={phoneInputRef}
            value={
              tel && /^\d{11}$/.test(tel) // 숫자 11자리인지 확인
                ? tel.replace(/(\d{3})(\d{4})(\d{4})/, "$1-$2-$3") // 포맷팅
                : tel || "" // 기본값 제공
            }
            onChange={handlePhoneChange}
            onVerify={handleSendSMS}
            isModalConfirmed={false}
            buttonPosition="below" // 버튼을 아래에 표시
            onFocus={handlePhoneInputFocus}
          />
        </div>
        {!isVerified ? (
          <div>
            {showVerificationModal && (
              <>
                <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center">
                  <div className="bg-white p-4 rounded-lg shadow-lg w-96">
                    <h2 className="text-xl font-bold mb-6">인증 코드 입력</h2>
                    <div className="flex items-center mb-3">
                      <div className="relative flex-1">
                        <input
                          type="text"
                          value={inputCode}
                          onChange={handleinputCodeChange}
                          maxLength={6}
                          className="w-full px-3 py-2 border border-gray-300 rounded-md"
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
                        onClick={() => setShowVerificationModal(false)}
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
                </div>
              </>
            )}
          </div>
        ) : (
          <div>
            <h2 className="text-xl font-bold mb-6">인증 완료</h2>
            <p>인증이 성공적으로 완료되었습니다.</p>
            <div className="flex justify-end">
              <button
                onClick={() => setShowVerificationModal(false)}
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
