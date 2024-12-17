import { useState, useEffect, forwardRef, useImperativeHandle, useRef } from "react";

interface PhoneInputProps {
  value: string; // 화면에 표시되는 값 (대시 포함)
  onChange: (formattedValue: string, rawValue: string) => void; // 두 값 전달
  onVerify: () => void;
  isModalConfirmed: boolean; // 인증 완료 상태
  buttonPosition?: "side" | "below"; // 버튼 위치 선택 prop
  onFocus?: () => void;
}

const PhoneInput = forwardRef<HTMLInputElement, PhoneInputProps>(
  ({ value, onChange, onVerify, isModalConfirmed, buttonPosition = "side",onFocus,}, ref) => {
    const [isVerified, setIsVerified] = useState(isModalConfirmed);

    // 내부 input의 ref
    const inputRef = useRef<HTMLInputElement | null>(null); // useRef로 변경

    // 외부에서 ref로 input을 사용할 수 있도록 연결
    useImperativeHandle(ref, () => inputRef.current as HTMLInputElement);

    useEffect(() => {
      if (buttonPosition === "side") {
        setIsVerified(isModalConfirmed);
      }
    }, [isModalConfirmed, buttonPosition]);

    const handlePhoneChange = (e: React.ChangeEvent<HTMLInputElement>) => {
      const input = e.target.value;
      const onlyNums = input.replace(/[^0-9]/g, ""); // 숫자만 추출

      let formattedInput = input;
      if (onlyNums.startsWith("010")) {
        if (onlyNums.length <= 3) {
          formattedInput = onlyNums;
        } else if (onlyNums.length <= 7) {
          formattedInput = `${onlyNums.slice(0, 3)}-${onlyNums.slice(3)}`;
        } else {
          formattedInput = `${onlyNums.slice(0, 3)}-${onlyNums.slice(3, 7)}-${onlyNums.slice(7, 11)}`;
        }
      } else {
        formattedInput = "010";
      }

      onChange(formattedInput, onlyNums); // 대시 포함 값과 대시 없는 값을 전달
    };

    const handleVerifyClick = () => {
      if (buttonPosition === "side") {
        setIsVerified(false); // 인증 시작 시 초기화
      }
      onVerify();
    };

    const isVerifyButtonEnabled = value.length === 13 && !isVerified;

    return buttonPosition === "side" ? (
      <div className="flex items-center space-x-1">
        <input
          id="tel"
          type="text"
          name="custom-tel"
          ref={inputRef}// 내부 input에 ref 연결
          value={value} // 대시 포함된 값
          onChange={handlePhoneChange}
          onFocus={onFocus}
          required
          maxLength={13}
          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:border-[#536DFE] focus:ring-0"
          placeholder="전화번호 입력 (010 제외)"
        />
        <button
          type="button"
          onClick={handleVerifyClick}
          disabled={!isVerifyButtonEnabled}
          className={`w-13 px-3 py-2 whitespace-nowrap rounded-md border border-gray-300 transition-colors ${
            isVerified
              ? "bg-gray-300 text-gray-500 border-gray-300 cursor-not-allowed"
              : isVerifyButtonEnabled
              ? "bg-[#536DFE] border-[#536DFE] text-white hover:bg-[#485acf]"
              : "bg-gray-300 text-gray-500 border-gray-300 cursor-not-allowed"
          }`}
        >
          {isVerified ? "인증완료" : "인증하기"}
        </button>
      </div>
    ) : (
      <div className="flex flex-col space-y-2">
        <input
          id="tel"
          type="text"
          name="custom-tel"
          ref={(el) => (inputRef.current = el)} // 내부 input에 ref 연결
          value={value}
          onChange={handlePhoneChange}
          required
          maxLength={13}
          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:border-[#536DFE] focus:ring-0"
          placeholder="전화번호 입력 (010 제외)"
        />
        <button
          onClick={handleVerifyClick}
          disabled={!isVerifyButtonEnabled}
          className={`w-full py-2 rounded-md transition-colors ${
            isVerifyButtonEnabled
              ? "bg-[#536DFE] text-white hover:bg-[#485acf]"
              : "bg-gray-300 text-gray-500 cursor-not-allowed"
          }`}
        >
          찾기
        </button>
      </div>
    );
  }
);

export default PhoneInput;