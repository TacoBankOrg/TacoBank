import React, { useState, useEffect } from "react";
import Keypad from "./PinKeypad";

interface TransferPinModalProps {
  isOpen: boolean;
  onClose: () => void;
  onPinEntered: (pin: string) => Promise<boolean>; // PIN 입력 완료 시 호출
  onAuthFallback: () => void; // 본인 인증으로 이동
  errorMessage?: string;
}

const TransferPinModal: React.FC<TransferPinModalProps> = ({
  isOpen,
  onClose,
  onPinEntered,
  onAuthFallback,
  errorMessage,
}) => {
  const [pin, setPin] = useState<string>(""); // PIN 입력 상태
  const [attemptsLeft, setAttemptsLeft] = useState<number>(5);
  const [step, setStep] = useState<number>(1); // Keypad의 단계 설정

  // PIN 입력 처리
  const handlePinChange = (digit: string) => {
    if (pin.length < 6) {
      const newPin = pin + digit;
      setPin(newPin);

      // 6자리 PIN 입력 완료 시 부모로 전달
      if (newPin.length === 6) {
        handleTransferPin(newPin); // PIN 검증 로직 호출
      }
    }
  };

  // PIN 검증 로직
  const handleTransferPin = async (enteredPin: string) => {
    const isCorrect = await onPinEntered(enteredPin);
  
    if (isCorrect) {
      // 성공 시 모달 닫기
      onClose();
    } else {
      const newAttemptsLeft = attemptsLeft - 1;
      setAttemptsLeft(newAttemptsLeft);
      setPin("");
  
      if (newAttemptsLeft === 0) {
        onClose();
        onAuthFallback();
      }
    }
  };

  // PIN 삭제 처리
  const handleDelete = () => {
    setPin(pin.slice(0, -1)); // PIN 마지막 자리 삭제
  };

  // 모달 초기화 처리
  useEffect(() => {
    if (!isOpen) {
      setPin(""); // PIN 초기화
      setAttemptsLeft(5); // 시도 횟수 초기화
      setStep(1); // Keypad 단계 초기화
    }
  }, [isOpen]);

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
        <h2 className="text-xl font-bold mb-4">송금 PIN 입력</h2>
        <div className="flex justify-center mb-6">
          {[...Array(6)].map((_, i) => (
            <div
              key={i}
              className={`w-4 h-4 mx-1 rounded-full ${
                pin.length > i ? "bg-blue-500" : "bg-gray-300"
              }`}
            ></div>
          ))}
        </div>
        {errorMessage && <p className="text-red-500 mb-2">{errorMessage}</p>}
        <Keypad
          onPinChange={handlePinChange}
          onDelete={handleDelete}
          showDelete={true}
          maxPinLength={6}
          currentPinLength={pin.length}
          step={step} // Keypad의 step 속성 추가
        />
      </div>
    </div>
  );
};

export default TransferPinModal;