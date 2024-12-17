import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import PasswordInput from "../../components/PasswordInput";
import backIcon from "../../assets/image/icon/backicon.png";
import { changePassword } from "../../api/memberApi";
import { ChangePasswordReq } from "../../types/memberTypes";
import useAuth from "../../hooks/useAuth";

const ChangePassword: React.FC = () => {
  const navigate = useNavigate();
  const { authData } = useAuth();
  const memberId = authData?.memberId;

  // State for old password, new password, and confirmation password
  const [oldPassword, setOldPassword] = useState("");
  const [password, setPassword] = useState("");
  const [rePassword, setRePassword] = useState("");

  const handleSubmit = async () => {
    try {
      if (!oldPassword) {
        alert("현재 비밀번호를 입력해주세요.");
        return;
      }

      if (password !== rePassword) {
        alert("비밀번호가 일치하지 않습니다.");
        return;
      }

      const dto: ChangePasswordReq = {
        memberId: memberId as number,
        currentPassword: oldPassword,
        newPassword: password,
        confirmPassword: rePassword,
      };

      console.log(dto);

      const response = await changePassword(dto);

      if (response.status === "SUCCESS") {
        alert("비밀번호가 성공적으로 변경되었습니다.");
        navigate("/my-info"); // 성공 시 이동
      }
    } catch (error) {
      console.error("API 요청 실패:", error);
      alert("비밀번호 변경 중 오류가 발생했습니다. 다시 시도해주세요.");
    }
  };

  return (
    <>
      {/* Header */}
      <header className="relative flex items-center mb-6">
        <button className="absolute left-0 p-1" onClick={() => navigate("/my-info")}>
          <img src={backIcon} alt="Back" className="w-8 h-8" />
        </button>
        <h1 className="text-3xl font-bold text-center w-full">
          비밀번호 변경
        </h1>
      </header>

      {/* Password Form */}
      <div>
        <PasswordInput
          password={password}
          rePassword={rePassword}
          onPasswordChange={setPassword}
          onRePasswordChange={setRePassword}
          onOldPasswordChange={setOldPassword}
          mode="change"
          onSubmit={handleSubmit}
        />
      </div>
    </>
  );
};

export default ChangePassword;
