import { useState, FormEvent, useEffect } from "react";
import backIcon from "../../assets/image/icon/backicon.png";
import SMSVerificationModal from "../../components/SMSVerificationModal";
import PhoneInput from "../../components/PhoneInput";
import PasswordInput from "../../components/PasswordInput";
import { useNavigate } from "react-router-dom";
import { Member } from "../../types/memberTypes";
import { useMutation } from "@tanstack/react-query";
import { checkEmailDuplicate, join } from "../../api/authApi";
import { sendSMS } from "../../api/memberApi";
import { SmsVerificationRequestDto } from "../../types/accountTypes";

function JoinPage() {
  const [members, setMembers] = useState<Member>({
    name: "",
    birth: "",
    gender: "",
    email: "",
    tel: "010",
    password: "",
  });
  const navigate = useNavigate();
  const [showVerificationModal, setShowVerificationModal] = useState(false);
  const [verificationId, setVerificationId] = useState<number | null>(null);
  const [isModalConfirmed, setIsModalConfirmed] = useState(false); // 인증 완료 여부 관리
  const [rePassword, setRePassword] = useState(""); // 비밀번호 확인
  const [emailError, setEmailError] = useState("");
  const [isEmailChecked, setIsEmailChecked] = useState(false);
  const [resendCooldown, setResendCooldown] = useState(0);
  const [birthError, setBirthError] = useState("");
  const mutation = useMutation({
    mutationFn: join, // 회원가입 API 함수
    onSuccess: () => {
      alert("회원가입이 성공적으로 완료되었습니다!");
      navigate("/login"); // 성공 시 로그인 페이지로 이동
    },
  });
  useEffect(() => {
    let timer: ReturnType<typeof setTimeout>;
    if (resendCooldown > 0) {
      timer = setTimeout(() => setResendCooldown(resendCooldown - 1), 1000);
    }
    return () => clearTimeout(timer);
  }, [resendCooldown]);

  // 전화번호 변경 핸들러
  const handlePhoneChange = (_formattedValue: string, rawValue: string) => {
    setMembers((prevUsers) => ({ ...prevUsers, tel: rawValue })); // 대시 없는 값 저장
  };

  const handleEmailDuplicateCheck = async () => {
    if (!members.email) {
      alert("이메일을 입력해주세요.");
      return;
    }
    try {
      await checkEmailDuplicate(members.email);

      // 중복 확인 성공 처리
      setIsEmailChecked(true);
      setEmailError("");// 성공 메시지 표시
      alert("사용 가능한 이메일 입니다.")
    } catch (error) {

        // 입력값 초기화 및 포커스 이동
        setMembers((prevUsers) => ({ ...prevUsers, email: "" }));
        setTimeout(() => {
          const emailInput = document.getElementById(
            "email"
          ) as HTMLInputElement;
          if (emailInput) {
            emailInput.focus();
          }
        }, 0);
      console.error("API 요청 실패", error);
    }
  };

  const handleEmailChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newEmail = e.target.value.trim(); // 앞뒤 공백 제거
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/; // 이메일 정규식

    setMembers((prevUsers) => ({ ...prevUsers, email: newEmail }));
    setIsEmailChecked(false); // 이메일 변경 시 중복 확인 상태 초기화

    if (!newEmail) {
      setEmailError(""); // 이메일이 비어 있으면 에러 제거
      return;
    }

    if (!emailRegex.test(newEmail)) {
      setEmailError("유효한 이메일 주소를 입력해주세요."); // 정규식 불일치 시 에러 설정
    } else {
      setEmailError(""); // 정규식 일치 시 에러 제거
    }
  };

  // 비밀번호 변경 핸들러
  const handlePasswordChange = (password: string) => {
    setMembers((prevMembers) => ({ ...prevMembers, password }));
  };

  // 비밀번호 확인 변경 핸들러
  const handleRePasswordChange = (rePassword: string) => {
    setRePassword(rePassword);
  };

  const handleSendSMS = async () => {
    try {
      if (!members || !members.tel) {
        alert("유효한 전화번호를 입력하세요."); // tel이 없는 경우 처리
        return;
      }

      const dto: SmsVerificationRequestDto = {
        tel: members.tel,
        type: "join",
      };
      const response = await sendSMS(dto);
      setVerificationId(response.verificationId); // 서버에서 반환된 인증 ID 저장
      setShowVerificationModal(true);
      setResendCooldown(180); // 타이머 초기화
    } catch (error) {
      console.error("API 요청 실패", error);
    }
  };

  // 인증 확인 핸들러
  const handleVerifyConfirm = () => {
    setShowVerificationModal(false);
    setIsModalConfirmed(true); // 인증 완료 후 버튼 비활성화 설정
  };

  // 폼 제출 핸들러
  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
  
    // 필수 입력 필드 검증
    if (!members.name?.trim()) {
      alert("이름을 입력해주세요.");
      return;
    }
    if (!isEmailChecked) {
      alert("이메일 중복 확인을 해주세요.");
      return;
    }
    if (!isModalConfirmed) {
      alert("전화번호 인증을 완료해주세요.");
      return;
    }
    if (members.password !== rePassword) {
      alert("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
      return;
    }
  
    // useMutation 호출
    mutation.mutate(members);
  };

  return (
    <div id="app-container" className="flex min-h-screen p-6">
      <header className="relative flex items-center justify-center mb-6">
        <button className="absolute left-0 p-1" onClick={() => navigate(-1)}>
          <img src={backIcon} alt="Back" className="w-8 h-8" />
        </button>
        <h1 className="text-3xl font-bold mx-auto">회원가입</h1>
      </header>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <input
            id="name"
            type="text"
            name="name"
            value={members.name}
            onChange={(e) => {
              const input = e.target.value;
              const filteredInput = input.replace(
                /[^a-zA-Zㄱ-ㅎㅏ-ㅣ가-힣]/g,
                ""
              ); // 한글, 영문만 허용
              setMembers({ ...members, name: filteredInput });
            }}
            required
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:border-[#536DFE] focus:ring-0"
            placeholder="이름"
          />
        </div>

        <div>
          <input
            id="birth"
            type="text"
            name="birth"
            value={members.birth + (members.gender ? `-${members.gender}` : "")}
            onChange={(e) => {
              const input = e.target.value.replace(/[^0-9]/g, ""); // 숫자만 허용
              if (input.length <= 7) {
                const isValid =
                  /^\d{2}([0]\d|[1][0-2])([0][1-9]|[1-2]\d|[3][0-1])[1-4]$/.test(
                    input
                  );
                const birthPart = input.slice(0, 6); // 생년월일 (앞 6자리)
                const genderPart = input.slice(6, 7); // 성별 구분 (1자리)
                setMembers({
                  ...members,
                  birth: birthPart,
                  gender: genderPart, // gender 저장
                });
                if (!isValid && input.length === 7) {
                  setBirthError("유효하지 않은 주민등록번호 형식입니다.");
                } else {
                  setBirthError(""); // 유효한 경우 에러 메시지 초기화
                }
              }
            }}
            required
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:border-[#536DFE] focus:ring-0"
            placeholder="주민등록번호 앞 7자리"
          />
          {birthError && (
            <p className="text-red-500 text-sm mt-1">{birthError}</p>
          )}
        </div>

        {/* Email Input */}
        <div>
        <div className="flex items-center space-x-1">
          <input
            id="email"
            type="email"
            name="email"
            value={members.email}
            onChange={handleEmailChange}
            required
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:border-[#536DFE] focus:ring-0"
            placeholder="이메일"
          />
          <button
            type="button"
            onClick={handleEmailDuplicateCheck}
            disabled={!members.email || !!emailError || isEmailChecked} // 확인 완료 시 비활성화
            className={`px-3 py-2 whitespace-nowrap rounded-md border border-gray-300 transition-colors ${
              members.email && !emailError && !isEmailChecked
                ? "bg-[#536DFE] border-[#536DFE] text-white hover:bg-[#485acf]"
                : "bg-gray-300 text-gray-500 border-gray-300 cursor-not-allowed"
            }`}
          >
            {isEmailChecked ? "확인완료" : "중복 확인"}
          </button>
        </div>
        {emailError && <p className="text-red-500 text-sm mt-1">{emailError}</p>}
        </div>

        <PhoneInput
          value={
            members.tel && /^\d{11}$/.test(members.tel) // 숫자 11자리인지 확인
              ? members.tel.replace(/(\d{3})(\d{4})(\d{4})/, "$1-$2-$3") // 포맷팅
              : members.tel || "" // 안전한 기본값 제공
          }
          onChange={handlePhoneChange}
          onVerify={handleSendSMS}
          isModalConfirmed={isModalConfirmed}
          buttonPosition="side"
        />

        {/* Password Input */}
        <PasswordInput
          password={members.password || ""}
          rePassword={rePassword}
          onPasswordChange={handlePasswordChange}
          onRePasswordChange={handleRePasswordChange}
          mode="join" // mode 설정
          email={members.email} // 이메일 전달
          birth={members.birth} // 생년월일 전달
          tel={members.tel} // 전화번호 전달
          onSubmit={() => {}} // 회원가입에서는 사용되지 않음
        />
        <button
          type="submit"
          className="w-full bg-[#536DFE] text-white py-2 rounded-md hover:bg-[#485acf] transition-colors"
        >
          회원가입
        </button>
      </form>

      <footer className="mt-6 text-center text-sm text-gray-600">
        이미 계정이 있으신가요?{" "}
        <a href="/login" className="text-[#536DFE] hover:underline">
          로그인
        </a>
      </footer>

      <SMSVerificationModal
        isOpen={showVerificationModal}
        onClose={() => setShowVerificationModal(false)}
        onVerifySuccess={handleVerifyConfirm}
        purpose="join"
        verificationId={verificationId}
        tel={members.tel || ""}
        resendCooldown={resendCooldown}
      />
    </div>
  );
}

export default JoinPage;
