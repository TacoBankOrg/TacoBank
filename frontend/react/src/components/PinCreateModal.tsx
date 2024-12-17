import React, { useState, useEffect } from "react";
import { createPIN } from "../api/homeApi";
import { PinReq } from "../types/homeTypes";
import Keypad from "./PinKeypad";
import useAuth from "../hooks/useAuth";

interface PinCreationModalProps {
  isOpen: boolean;
  onClose: () => void;
  onPinCreated: () => void; // PIN 생성 성공 시 호출
}

const PinCreationModal: React.FC<PinCreationModalProps> = ({
  isOpen,
  onClose,
  onPinCreated,
}) => {
  const { authData } = useAuth(); // useAuth 훅을 통해 사용자 정보 가져오기
  const memberId = authData?.memberId
  const [pinStep, setPinStep] = useState<1 | 2>(1); // PIN 단계 (1: PIN 입력, 2: 검증)
  const [pin, setPin] = useState<string>(""); // 1차 PIN
  const [confirmPin, setConfirmPin] = useState<string>(""); // 2차 PIN
  const [errorMessage, setErrorMessage] = useState<string>("");

  const handlePinChange = (digit: string) => {
    if (pinStep === 1 && pin.length < 6) {
      setPin(pin + digit);
    } else if (pinStep === 2 && confirmPin.length < 6) {
      setConfirmPin(confirmPin + digit);
    }
  };

  const handleDelete = () => {
    if (pinStep === 1) {
      setPin(pin.slice(0, -1));
    } else if (pinStep === 2) {
      setConfirmPin(confirmPin.slice(0, -1));
    }
  };

  useEffect(() => {
    if (pin.length === 6 && pinStep === 1) {
      setPinStep(2); // PIN 입력 완료 -> 검증 단계로 이동
    }

    if (confirmPin.length === 6 && pinStep === 2) {
      if (pin !== confirmPin) {
        setErrorMessage("PIN이 일치하지 않습니다. 다시 입력하세요.");
        setPin("");
        setConfirmPin("");
        setPinStep(1); // 다시 PIN 입력 단계로 이동
      } else {
        handleCreatePin(pin); // PIN 생성 요청
      }
    }
  }, [pin, confirmPin]);
  const handleCreatePin = async (pin: string) => {
    try {
      const dto: PinReq = { memberId: memberId as number, transferPin: pin, confirmTransferPin: confirmPin };
      console.log(dto)
      await createPIN(dto); // 서버에 PIN 생성 요청
      alert("PIN 생성이 완료되었습니다.");
      onPinCreated(); // 성공 콜백 호출
    } catch (error) {
      console.error("PIN 생성 실패:", error);
      setErrorMessage("PIN 생성에 실패했습니다. 다시 시도하세요.");
      setPin("");
      setConfirmPin("");
      setPinStep(1); // PIN 입력 단계로 돌아가기
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center">
      <div className="bg-white p-6 rounded-lg shadow-lg w-96 relative">
        <button
          onClick={onClose}
          className="absolute top-2 right-2 text-gray-500 hover:text-gray-700"
        >
          &times;
        </button>
        <h2 className="text-xl font-bold mb-4">
          {pinStep === 1 ? "PIN 입력" : "PIN 확인"}
        </h2>
        {errorMessage && <p className="text-red-500 mb-2">{errorMessage}</p>}
        <div className="flex justify-center mb-6">
          {[...Array(6)].map((_, i) => (
            <div
              key={i}
              className={`w-4 h-4 mx-1 rounded-full ${
                (pinStep === 1 ? pin.length : confirmPin.length) > i
                  ? "bg-blue-500"
                  : "bg-gray-300"
              }`}
            ></div>
          ))}
        </div>
        <Keypad
          onPinChange={handlePinChange}
          onDelete={handleDelete}
          showDelete={true}
          step={pinStep}
          maxPinLength={6}
          currentPinLength={pinStep === 1 ? pin.length : confirmPin.length}
        />
      </div>
    </div>
  );
};

export default PinCreationModal;