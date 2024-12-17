import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import backIcon from "../../assets/image/icon/backicon.png";
import Keypad from "../../components/PinKeypad";
import SMSVerificationModal from "../../components/SMSVerificationModal";
import { changePin, currentPin, resetPin } from "../../api/memberApi"; // API 호출 함수 import
import {
  ChangePinReq,
  SetPinRequestDto,
  validatePin,
} from "../../types/memberTypes"; // DTO import
import useAuth from "../../hooks/useAuth";

const ChangePin: React.FC = () => {
  const navigate = useNavigate();
  const [step, setStep] = useState<1 | 2 | 3 | 4>(1); // 1: 현재 PIN, 2: 새 PIN, 3: PIN 확인, 4: SMS 인증
  const [currentPinInput, setCurrentPinInput] = useState("");
  const [newPin, setNewPin] = useState("");
  const [confirmNewPin, setConfirmNewPin] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [incorrectAttempts, setIncorrectAttempts] = useState(0);
  const [showSmsModal, setShowSmsModal] = useState(false);
  const [resetMode, setResetMode] = useState(false); // true면 SMS 인증 후 PIN 재설정 모드
  const { member, authData } = useAuth();
  const memberId = member?.memberId;

  useEffect(() => {
    if (authData?.mydataLinked !== "Y") {
      navigate("/my-info"); // 홈 화면으로 리다이렉트
    }
  }, [authData, navigate]);


  const handlePinChange = (number: string) => {
    if (step === 1 && currentPinInput.length < 6 && !resetMode) {
      setCurrentPinInput((prev) => {
        const updatedPin = prev + number;
        if (updatedPin.length === 6) {
          setTimeout(async () => {
            try {
              const dto: validatePin = {
                memberId: Number(memberId),
                transferPin: updatedPin,
              };
              const response = await currentPin(dto);
              console.log(response);
              if (response.status === "SUCCESS") {
                setStep(2); // 새 PIN 입력 단계로 이동
                setErrorMessage("");
              } else {
                throw new Error();
              }
            } catch {
              const updatedAttempts = incorrectAttempts + 1;
              setIncorrectAttempts(updatedAttempts);

              if (updatedAttempts >= 5) {
                alert("5회 오류 발생. SMS 인증을 진행 후 PIN을 재설정하세요.");
                setShowSmsModal(true);
                setResetMode(true); // PIN 재설정 모드 활성화
                setStep(4); // SMS 인증 단계로 이동
              } else {
                setErrorMessage(
                  `현재 PIN이 일치하지 않습니다 (${updatedAttempts}/5)`
                );
                setCurrentPinInput(""); // PIN 초기화
              }
            }
          }, 100);
        }
        return updatedPin;
      });
    } else if (step === 2 && newPin.length < 6) {
      setNewPin((prev) => {
        const updatedPin = prev + number;
        if (updatedPin.length === 6) {
          setStep(3); // PIN 확인 단계로 이동
          setErrorMessage("");
        }
        return updatedPin;
      });
    } else if (step === 3 && confirmNewPin.length < 6) {
      setConfirmNewPin((prev) => {
        const updatedPin = prev + number;
        if (updatedPin.length === 6) {
          setTimeout(async () => {
            if (updatedPin === newPin) {
              try {
                if (resetMode) {
                  // SMS 인증 후 PIN 재설정
                  const dto: SetPinRequestDto = {
                    memberId: Number(memberId),
                    transferPin: newPin,
                    confirmTransferPin: updatedPin,
                  };

                  const response = await resetPin(dto);
                  console.log(response);
                  if (response.status === "SUCCESS") {
                    alert("PIN이 성공적으로 재설정되었습니다.");
                    navigate("/my-info"); // My Info로 이동
                  } else {
                    throw new Error();
                  }
                } else {
                  // 기존 PIN 변경
                  const dto: ChangePinReq = {
                    memberId: Number(memberId),
                    currentPin: currentPinInput,
                    newPin: newPin,
                    confirmPin: updatedPin,
                  };
                  const response = await changePin(dto);

                  if (response.status === "SUCCESS") {
                    alert("PIN이 성공적으로 변경되었습니다.");
                    navigate("/my-info"); // My Info로 이동
                  } else {
                    throw new Error();
                  }
                }
              } catch (error) {
                console.error("PIN 변경/재설정 오류:", error);
                setErrorMessage(
                  "PIN 변경/재설정에 실패했습니다. 나중에 다시 시도해주세요."
                );
              }
            } else {
              setErrorMessage("PIN이 일치하지 않습니다. 다시 입력해주세요.");
              setNewPin(""); // 새 PIN 초기화
              setConfirmNewPin(""); // 검증 PIN 초기화
              setStep(2); // 새 PIN 입력 단계로 되돌아감
            }
          }, 100);
        }
        return updatedPin;
      });
    }
  };

  const handleForgotPin = () => {
    alert("비밀번호를 잊으셨나요? SMS 인증을 진행 후 PIN을 재설정합니다.");
    setShowSmsModal(true);
    setResetMode(true); // PIN 재설정 모드 활성화
    setStep(4); // SMS 인증 단계로 이동
  };

  const handleDelete = () => {
    if (step === 1) setCurrentPinInput(currentPinInput.slice(0, -1));
    else if (step === 2) setNewPin(newPin.slice(0, -1));
    else if (step === 3) setConfirmNewPin(confirmNewPin.slice(0, -1));
  };

  const handleSmsVerificationSuccess = () => {
    setShowSmsModal(false);
    setStep(2); // SMS 인증 성공 후 새 PIN 입력 단계로 이동
    setErrorMessage("");
    setCurrentPinInput("");
    setNewPin("");
    setConfirmNewPin("");
    setIncorrectAttempts(0);
  };

  const handleSmsClose = () => {
    setShowSmsModal(false);
    navigate("/my-info"); // SMS 창 닫으면 My Info로 이동
  };

  return (
    <>
      <header className="relative flex items-center p-4">
        <button className="p-1" onClick={() => navigate("/my-info")}>
          <img src={backIcon} alt="Back" className="w-8 h-8" />
        </button>
      </header>

      <div className="flex flex-col items-center justify-center">
        <p className="text-center mb-8">
          {errorMessage ||
            (step === 1
              ? "현재 PIN을 입력하세요"
              : step === 2
              ? "새 PIN을 입력하세요"
              : step === 3
              ? "새 PIN을 확인하세요"
              : "SMS 인증을 진행해주세요")}
        </p>

        <div className="flex space-x-4 mb-6">
          {[...Array(6)].map((_, index) => (
            <div
              key={index}
              className={`w-6 h-6 border-2 rounded-full ${
                index <
                (step === 1
                  ? currentPinInput.length
                  : step === 2
                  ? newPin.length
                  : confirmNewPin.length)
                  ? "bg-gray-700"
                  : "bg-transparent"
              }`}
            ></div>
          ))}
        </div>

        {step === 1 && (
          <button
            onClick={handleForgotPin}
            className="px-4 py-2 bg-[#536DFE] text-white rounded-md mb-4"
          >
            비밀번호를 잊으셨나요?
          </button>
        )}
      </div>

      <div className="flex flex-col items-center justify-end p-6">
        <Keypad
          onPinChange={handlePinChange}
          onDelete={handleDelete}
          maxPinLength={6}
          showDelete={
            (step === 1 && currentPinInput.length > 0) ||
            (step === 2 && newPin.length > 0) ||
            (step === 3 && confirmNewPin.length > 0)
          }
          step={step}
          currentPinLength={0}
        />
      </div>

      {showSmsModal && (
        <SMSVerificationModal
          isOpen={showSmsModal}
          onClose={handleSmsClose}
          onVerifySuccess={handleSmsVerificationSuccess}
          purpose="pin"
        />
      )}
    </>
  );
};

export default ChangePin;
