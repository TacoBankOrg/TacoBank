import { useState, useRef } from "react";
import EyeOpenIcon from "../assets/image/icon/EyeOpenIcon";
import EyeClosedIcon from "../assets/image/icon/EyeClosedIcon";
import CheckIcon from "../assets/image/icon/CheckIcon";

interface PasswordInputProps {
  password: string;
  rePassword: string;
  onPasswordChange: (password: string) => void;
  onRePasswordChange: (rePassword: string) => void;
  onOldPasswordChange?: (oldPassword: string) => void;
  mode: "join" | "reset" | "change"; // 회원가입, 비번 찾기, 비번 재설정
  onSubmit?: () => void;
  email?: string; // 이용자 식별부호
  birth?: string; // 생년월일
  tel?: string; // 전화번호
}

export default function PasswordInput({
  password,
  rePassword,
  onPasswordChange,
  onRePasswordChange,
  onOldPasswordChange,
  mode,
  onSubmit,
  email,
  birth,
  tel,
}: PasswordInputProps) {
  const [showOldPassword, setShowOldPassword] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showRePassword, setShowRePassword] = useState(false);
  const [showErrorOnFocus, setShowErrorOnFocus] = useState(false);
  const [passwordChecklist, setPasswordChecklist] = useState({
    length: false,
    specialChars: false,
    noSequentialOrRepeatingNumbers: false,
  });
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const passwordRef = useRef<HTMLInputElement>(null);
  const rePasswordRef = useRef<HTMLInputElement>(null);
  const oldPasswordRef = useRef<HTMLInputElement>(null); 

  const toggleOldPasswordVisibility = (event: React.MouseEvent) => {
    event.preventDefault();
    setShowOldPassword(!showOldPassword);
    if (oldPasswordRef.current) {
      const cursorPosition = oldPasswordRef.current.selectionStart;
      setTimeout(() => {
        oldPasswordRef.current?.focus();
        oldPasswordRef.current?.setSelectionRange(cursorPosition, cursorPosition);
      }, 0); // 커서 위치 유지
    }
  };

  const togglePasswordVisibility = (event: React.MouseEvent) => {
    event.preventDefault();
    setShowPassword(!showPassword);
    if (passwordRef.current) {
      const cursorPosition = passwordRef.current.selectionStart;
      setTimeout(() => {
        passwordRef.current?.focus();
        passwordRef.current?.setSelectionRange(cursorPosition, cursorPosition);
      }, 0); // 커서 위치 유지
    }
  };

  const toggleRePasswordVisibility = (event: React.MouseEvent) => {
    event.preventDefault();
    setShowRePassword(!showRePassword);
    if (rePasswordRef.current) {
      const cursorPosition = rePasswordRef.current.selectionStart;
      setTimeout(() => {
        rePasswordRef.current?.focus();
        rePasswordRef.current?.setSelectionRange(
          cursorPosition,
          cursorPosition
        );
      }, 0); // 커서 위치 유지
    }
  };

  const validatePassword = (password: string) => {
    const passwordRegex =/^[A-Za-z\d!@_]{8,16}$/;
    const emailUserPart = email ? email.split("@")[0] : "";
    const birthNumbers = birth ? birth.replace(/-/g, "") : "";
    const telNumbers = tel ? tel.replace(/-/g, "") : "";

    // 개인정보 포함 여부
    const hasThreeConsecutiveSubstring = (source: string, target: string) => {
      if (!source || !target) return false;
      for (let i = 0; i <= source.length - 3; i++) {
        const substring = source.slice(i, i + 3); // 3자리씩 슬라이싱
        if (target.includes(substring)) {
          return true;
        }
      }
      return false;
    };

    const matchesBirthPattern = (birth: string, password: string): boolean => {
      if (!birth) return false;
      const yy = birth.slice(0,2)
      const mmdd = birth.slice(2, 6); // MMDD 추출
      const yymmdd = birth.slice(0, 6); // YYMMDD 추출
  
      return password.includes(yy) || password.includes(mmdd) || password.includes(yymmdd);
    };

    const containsEmailPart = hasThreeConsecutiveSubstring(
      emailUserPart.toLowerCase(),
      password.toLowerCase()
    );
    const containsTelNumbers = hasThreeConsecutiveSubstring(
      telNumbers,
      password
    );

    const containsBirthNumbers = matchesBirthPattern(
      birthNumbers,
      password
    );

    // 에러 메시지 처리
    if (containsEmailPart || containsTelNumbers || containsBirthNumbers) {
      setErrorMessage("비밀번호에 개인정보를 포함할 수 없습니다.");
      setPasswordChecklist({
        length: false,
        specialChars: false,
        noSequentialOrRepeatingNumbers: false,
      });
      return;
    }
    if (!password) {
      setErrorMessage(null);
      setPasswordChecklist({
        length: false,
        specialChars: false,
        noSequentialOrRepeatingNumbers: false,
      });
      return;
    }

    if (!passwordRegex.test(password)) {
      setErrorMessage(
        "비밀번호는 8자 이상, 16자 이하로 입력해야 합니다.");
      setPasswordChecklist({
        length: false,
        specialChars: false,
        noSequentialOrRepeatingNumbers: false,
      });
      return;
    }
    
    // 연속/반복 숫자 검사
    const containsSequentialOrRepeatingNumbers =
      /(012|123|234|345|456|567|678|789|890)/.test(password) || // 순차 검사
      /(210|321|432|543|654|765|876|987|098)/.test(password) || // 역순 검사
      /(\d)\1{2,}/.test(password); // 반복 숫자 검사

    // 기본 조건 평가
    const containsLetters = /[A-Za-z]/.test(password);
    const containsNumbers = /[0-9]/.test(password);
    const containsAllowedSpecialChars = /[!@_]/.test(password);
    const containsInvalidSpecialChars = /[^A-Za-z\d!@_]/.test(password);
    const isValidLength = password.length >= 8;

    const checklist = {
      length:
        isValidLength &&
        containsLetters &&
        containsNumbers &&
        containsAllowedSpecialChars,
      specialChars: containsAllowedSpecialChars && !containsInvalidSpecialChars,
      noSequentialOrRepeatingNumbers: !containsSequentialOrRepeatingNumbers,
    };
    setPasswordChecklist(checklist);

    const allConditionsMet =
      checklist.length &&
      checklist.specialChars &&
      checklist.noSequentialOrRepeatingNumbers;

    if (allConditionsMet) {
      setErrorMessage(null); // 모든 조건 충족 시 에러 메시지 제거
    } else {
      setErrorMessage(null); // 실시간 체크리스트는 에러 메시지 제거
    }

    if (allConditionsMet) {
      setErrorMessage(null);
      return; // 체크리스트도 숨김
    }

    if (mode !== "join") {
      // 에러 메시지 업데이트 (회원가입 모드가 아닌 경우)
      if (!checklist.length) {
        setErrorMessage("영문자, 숫자, 특수문자를 포함해야 합니다.");
      } else if (!checklist.specialChars) {
        setErrorMessage("허용된 특수문자(!@_)만 사용할 수 있습니다.");
      } else if (!checklist.noSequentialOrRepeatingNumbers) {
        setErrorMessage("연속된 숫자나 반복된 숫자를 사용할 수 없습니다.");
      } else {
        setErrorMessage(null); // 모든 조건을 충족하면 에러 메시지 제거
      }
    }
  };

  const handlePasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newPassword = e.target.value;

    if (!newPassword) {
      setErrorMessage(null);
      setPasswordChecklist({
        length: false,
        specialChars: false,
        noSequentialOrRepeatingNumbers: false,
      });
    }
    
    onPasswordChange(newPassword);

    validatePassword(newPassword);

    // 회원가입 모드가 아닌 경우, 포커스가 없을 때 에러 메시지를 숨김
    if (mode !== "join" && !showErrorOnFocus) {
      setErrorMessage(null);
    }
  };

  const handleRePasswordFocus = () => {
    setShowErrorOnFocus(true); // 비밀번호 확인 필드에 포커스 시 에러 메시지 표시
  };

  const isPasswordMatching = password === rePassword && rePassword.length > 0;

  return (
    <>
      {mode === "change" && (
        <div className="relative mb-4">
          <div className="relative items-center border border-gray-300 rounded-md focus:outline-none focus-within:border-[#536DFE]">
            <input
              id="oldPassword"
              type={showOldPassword ? "text" : "password"}
              name="oldPassword"
              onChange={(e) => onOldPasswordChange?.(e.target.value)}
              required
              className="w-full px-3 py-2 pr-10 outline-none rounded-md" 
              placeholder="현재 비밀번호를 입력하세요"
            />
            <button
              type="button"
      className="absolute right-3 top-1/2 transform -translate-y-1/2 cursor-pointer"
              onClick={toggleOldPasswordVisibility}
            >
              {showOldPassword ? <EyeOpenIcon /> : <EyeClosedIcon />}
            </button>
          </div>
        </div>
      )}

      <div className="relative">
        <div className="relative items-center border border-gray-300 rounded-md focus:outline-none focus-within:border-[#536DFE]">
          <input
            id="password"
            ref={passwordRef}
            type={showPassword ? "text" : "password"}
            name="password"
            value={password}
            onChange={handlePasswordChange}
            required
            className="w-full px-3 py-2 pr-10 outline-none rounded-md" 
            placeholder={
              mode === "join" ? "비밀번호(8자 이상)" : "새 비밀번호(8자 이상)"
            }
          />
          <button
            type="button"
              className="absolute right-3 top-1/2 transform -translate-y-1/2 cursor-pointer"
            onClick={togglePasswordVisibility}
          >
            {showPassword ? <EyeOpenIcon /> : <EyeClosedIcon />}
          </button>
        </div>

        {mode === "join" && (
          <>
            {errorMessage && (
              <p className="text-red-500 text-xs mt-1">{errorMessage}</p>
            )}

            {/* 체크리스트: 개인정보 에러가 없을 때만 표시 */}
            {!errorMessage && (
              <div className="mt-2">
                {[
                  {
                    condition: passwordChecklist.length,
                    label: "영문자, 숫자, 특수문자 포함",
                  },
                  {
                    condition: passwordChecklist.specialChars,
                    label: "허용된 특수문자: !@_",
                  },
                  {
                    condition: passwordChecklist.noSequentialOrRepeatingNumbers,
                    label: "연속/반복 숫자 불가",
                  },
                ].map(({ condition, label }, index) => (
                  <p key={index} className="flex items-center">
                    <CheckIcon isActive={condition} />
                    <span
                      className={`text-sm ${
                        condition ? "text-green-500" : "text-gray-400"
                      }`}
                    >
                      {label}
                    </span>
                  </p>
                ))}
              </div>
            )}
          </>
        )}

        {/* 회원가입 외 모드에서 에러 메시지 표시 */}
        {mode !== "join" && showErrorOnFocus && errorMessage && (
          <p className="text-red-500 text-xs mt-1">{errorMessage}</p>
        )}
      </div>

      <div className="relative mt-4">
        <div className="relative items-center border border-gray-300 rounded-md focus:outline-none focus-within:border-[#536DFE]">
          <input
            id="RePassword"
            ref={rePasswordRef}
            type={showRePassword ? "text" : "password"}
            name="RePassword"
            value={rePassword}
            onChange={(e) => onRePasswordChange(e.target.value)}
            onFocus={handleRePasswordFocus}
            required
            className={`flex-grow px-3 py-2 outline-none rounded-md ${
              isPasswordMatching ? "border-green-500" : "border-gray-300"
            }`}
            placeholder="비밀번호 확인"
          />
          <button
            type="button"
                className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500 hover:text-black"
            onClick={toggleRePasswordVisibility}
          >
            {showRePassword ? <EyeOpenIcon /> : <EyeClosedIcon />}
          </button>
        </div>
        {!isPasswordMatching && rePassword.length > 0 && (
          <p className="text-red-500 text-xs mt-1">
            비밀번호가 일치하지 않습니다.
          </p>
        )}
      </div>

      {(mode === "reset" || mode === "change") && (
        <button
          type="button"
          onClick={onSubmit}
          disabled={!isPasswordMatching}
          className={`mt-4 w-full py-2 rounded-md ${
            isPasswordMatching
              ? "bg-[#536DFE] text-white"
              : "bg-gray-300 text-gray-400 cursor-not-allowed"
          }`}
        >
          확인
        </button>
      )}
    </>
  );
}
