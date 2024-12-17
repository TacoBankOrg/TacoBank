import { useState } from "react";
import PasswordInput from "../../components/PasswordInput";
import backIcon from "../../assets/image/icon/backicon.png";
import { useLocation, useNavigate } from "react-router-dom";
import { findPasswordCheck } from "../../api/memberApi";
import { FindPwCheckReqDto } from "../../types/memberTypes";

export default function ResetPassword() {
  const location = useLocation();
  const memberId = location.state?.memberId;
  const navigate = useNavigate();

  // 비밀번호와 비밀번호 확인 상태 관리
  const [password, setPassword] = useState("");
  const [rePassword, setRePassword] = useState("");

  // 비밀번호 변경 핸들러
  const handlePasswordChange = (newPassword: string) => {
    setPassword(newPassword);
  };

  // 비밀번호 확인 변경 핸들러
  const handleRePasswordChange = (newRePassword: string) => {
    setRePassword(newRePassword);
  };

  // 비밀번호 재설정 완료 버튼 클릭 핸들러
  const handleSubmit = async () => {
    try {
      const dto: FindPwCheckReqDto = {
        memberId,
        newPassword: password,
        confirmPassword: rePassword,
      };
      console.log(dto);
      const response = await findPasswordCheck(dto);
      console.log(response);
      if (response.status === "SUCCESS") {
        alert(response.message || "비밀번호 재설정이 완료되었습니다.");
        navigate("/login");
      } else {
        alert(response.message || "실패 했습니다");
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
        <h1 className="text-3xl font-bold text-cyan-500 mx-auto">
          비밀번호 초기화
        </h1>
      </div>

      <PasswordInput
        password={password}
        rePassword={rePassword}
        onPasswordChange={handlePasswordChange}
        onRePasswordChange={handleRePasswordChange}
        mode="reset" // 비밀번호 초기화 모드
        onSubmit={handleSubmit} // 초기화 버튼 동작
      />
    </div>
  );
}