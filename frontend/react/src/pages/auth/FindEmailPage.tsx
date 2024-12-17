import { useState } from "react";
import backIcon from "../../assets/image/icon/backicon.png";
import { useNavigate } from "react-router-dom";
import PhoneInput from "../../components/PhoneInput";
import { findEmail } from "../../api/memberApi";

export default function FindEmail() {
  const navigate = useNavigate();
  const [tel, setTel] = useState("010"); // 전화번호 상태 추가

  const handlePhoneChange = (_formattedValue: string, rawValue: string) => {
    setTel(rawValue);
  };

  const handleVerifySuccess = async () => {
    try {
      const response = await findEmail(tel); // API 호출로 이메일 조회
  console.log(response)
      // 응답 구조 확인 및 성공 여부 체크
      if (response.status === "SUCCESS") {
        const email = response.data.email; // 이메일 데이터 추출
        if (typeof email === "string") {
          navigate("/email-result", { state: { email } }); // 성공 시 결과 페이지로 이동
        } else {
          throw new Error("서버에서 잘못된 데이터 형식을 반환했습니다."); // 잘못된 데이터 처리
        }
      } else {
        throw new Error(response.data?.message || "이메일 조회에 실패했습니다."); // 실패 메시지 처리
      }
    } catch (error) {
      console.error("API 요청 실패", error);
    }
  };

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
          이메일 찾기
        </h1>
      </div>

      {/* 탭 전환 버튼 */}
      <div className="flex justify-center mb-6 border-b border-gray-300">
        <button
          className="flex-1 py-2 text-center text-[#536DFE] border-b-2 border-[#536DFE] font-bold"
          onClick={() => navigate("/find-email")}
        >
          이메일 찾기
        </button>
        <button
          className="flex-1 py-2 text-center text-gray-500"
          onClick={() => navigate("/find-password")}
        >
          비밀번호 찾기
        </button>
      </div>

      {/* 전화번호 입력 필드와 찾기 버튼 */}
      <div className="space-y-4">
        <PhoneInput
          value={
            tel && /^\d{11}$/.test(tel) // 숫자 11자리인지 확인
              ? tel.replace(/(\d{3})(\d{4})(\d{4})/, "$1-$2-$3") // 포맷팅
              : tel || "" // 기본값 제공
          }
          onChange={handlePhoneChange}
          onVerify={handleVerifySuccess}
          isModalConfirmed={false}
          buttonPosition="below" // 버튼을 아래에 표시
        />
      </div>
    </div>
  );
}
