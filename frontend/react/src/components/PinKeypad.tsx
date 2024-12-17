import React, { useEffect, useState } from "react";

interface KeypadProps {
  onPinChange: (pin: string) => void; // PIN 변경 핸들러
  maxPinLength: number; // 최대 PIN 길이 (예: 6)
  onDelete: () => void; // PIN 삭제
  showDelete: boolean;
  step: number;
  currentPinLength: number; // 현재 입력된 PIN 길이
}

const Keypad: React.FC<KeypadProps> = ({
  onPinChange,
  onDelete,
  showDelete,
  step,
  maxPinLength,
  currentPinLength,
}) => {
  const [shuffledNumbers, setShuffledNumbers] = useState<string[]>([]);
  const [highlightedKeys, setHighlightedKeys] = useState<string[]>([]);

  // 숫자 셔플
  const shuffleNumbers = () => {
    const numbers = ["1", "2", "3", "4", "5", "6", "7", "8", "9", "0"];
    for (let i = numbers.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [numbers[i], numbers[j]] = [numbers[j], numbers[i]];
    }
    setShuffledNumbers(numbers);
  };

  // 컴포넌트 로드 시 셔플
  useEffect(() => {
    shuffleNumbers();
  }, [step]);

  // 숫자 클릭 핸들러
  const handleNumberClick = (number: string) => {
    if (currentPinLength >= maxPinLength) return; // PIN이 최대 길이이면 클릭 비활성화
    onPinChange(number);

    // 랜덤 하이라이트 설정
    const randomNumbers = shuffledNumbers
      .filter((key) => key !== number)
      .sort(() => Math.random() - 0.5)
      .slice(0, 2);

    setHighlightedKeys([number, ...randomNumbers]);

    setTimeout(() => {
      setHighlightedKeys([]);
    }, 50);
  };

  return (
    <div className="grid grid-cols-3 gap-6">
      {shuffledNumbers.slice(0, 9).map((key, index) => (
        <button
          key={index}
          onClick={() => handleNumberClick(key)}
          disabled={currentPinLength >= maxPinLength} // PIN이 최대 길이일 때 비활성화
          className={`text-gray-700 text-2xl font-bold w-20 h-20 flex items-center justify-center rounded-full ${
            highlightedKeys.includes(key) ? "bg-[#EAEDFF]" : "bg-transparent"
          } ${currentPinLength >= maxPinLength ? "cursor-not-allowed opacity-50" : ""}`}
        >
          {key}
        </button>
      ))}
      <div className="w-20 h-20"></div>
      <button
        onClick={() => handleNumberClick(shuffledNumbers[9])}
        disabled={currentPinLength >= maxPinLength} // PIN이 최대 길이일 때 비활성화
        className={`text-gray-700 text-2xl font-bold w-20 h-20 flex items-center justify-center rounded-full ${
          highlightedKeys.includes(shuffledNumbers[9])
            ? "bg-[#EAEDFF]"
            : "bg-transparent"
        } ${currentPinLength >= maxPinLength ? "cursor-not-allowed opacity-50" : ""}`}
      >
        {shuffledNumbers[9]}
      </button>
      {showDelete && (
        <button
          onClick={onDelete}
          className="text-gray-700 text-2xl font-bold w-20 h-20 rounded-full"
        >
          ←
        </button>
      )}
    </div>
  );
};

export default Keypad;
