import React, { useEffect, useState } from "react";
import { Outlet, useNavigate } from "react-router-dom";
import backIcon from "../../assets/image/icon/backicon.png";
import {
  SmsConfirmRequestDto,
  SmsVerificationRequestDto,
} from "../../types/accountTypes";
import {
  checkSMS,
  deactivation,
  editMember,
  getMember,
  sendSMS,
} from "../../api/memberApi";
import { MemberData, UpdateMemberInfoReq } from "../../types/memberTypes";
import useAuth from "../../hooks/useAuth";
import { logout } from "../../api/authApi";
import axios from "axios";
import { handleApiError } from "../../utils/apiHelpers";
import BottomNav from "../../components/BottomNav";

const MyInfo: React.FC = () => {
  const { authData, removeMember } = useAuth();
  const navigate = useNavigate();

  const [memberData, setMemberData] = useState<MemberData>();
  const memberId = authData?.memberId;
  const [isEditing, setIsEditing] = useState(false);
  const [verificationCode, setVerificationCode] = useState("");
  const [isVerified, setIsVerified] = useState(false);
  const [verificationId, setVerificationId] = useState<number | null>(null); // 인증 ID
  const [resendCooldown, setResendCooldown] = useState(180);
  const isMyinfo = location.pathname === "/my-info";
  const [updatedPhone, setUpdatedPhone] = useState<string>(""); // 기본값 설정

  useEffect(() => {
    if (memberData) {
      setUpdatedPhone(memberData.tel);
    }
  }, [memberData]);

  // 사용자 정보 요청
  useEffect(() => {
    const fetchMemberData = async () => {
      try {
        let response = await getMember(memberId as number);
        console.log(response);
        if (response.status && response.status === "SUCCESS") {
          response = response.data;

          setMemberData(response);
          setUpdatedPhone(response.tel);
        } else {
          // 사용자 정보 조회 실패시
          alert(response.message);
          navigate("/"); // 홈으로 이동
        }
      } catch (error) {
        console.error("사용자 정보를 가져오는 중 오류 발생:", error);
      }
    };

    fetchMemberData();
  }, [memberId]);

  // 타이머 관리
  useEffect(() => {
    let timer: ReturnType<typeof setTimeout>;
    if (resendCooldown > 0) {
      timer = setTimeout(() => setResendCooldown(resendCooldown - 1), 1000);
    }
    return () => clearTimeout(timer);
  }, [resendCooldown]);

  const handleSendVerification = async () => {
    try {
      if (updatedPhone.length !== 11) {
        alert("전화번호를 올바르게 입력해주세요.");
        return;
      }

      const dto: SmsVerificationRequestDto = {
        tel: updatedPhone,
        type: "tel",
      };
      const response = await sendSMS(dto);

      setVerificationId(response.verificationId); // 인증 ID 설정
      setResendCooldown(180); // 타이머 시작
      alert("인증번호가 발송되었습니다.");
    } catch (error) {
      if (axios.isAxiosError(error)) {
        const errorMessage = handleApiError(error); // 에러 메시지 처리
        alert(errorMessage);
      } else {
        alert("인증번호 발송에 실패했습니다. 다시 시도해주세요.");
      }
    }
  };

  const handleVerifyCode = async () => {
    try {
      if (!verificationId) {
        alert("인증번호를 먼저 요청하세요.");
        return;
      }
      const dto: SmsConfirmRequestDto = {
        type: "tel",
        verificationId,
        tel: updatedPhone,
        inputCode: verificationCode,
      };
      const response = await checkSMS(dto);
      if (response.status === "SUCCESS") {
        alert("인증이 완료되었습니다.");

        const dto: UpdateMemberInfoReq = {
          name: memberData?.name,
          tel: updatedPhone,
        };
        //정보변경요청
        const response = await editMember(dto, memberId as number);
        if (response.status === "SUCCESS") {
          alert("정보가 정상적으로 변경되었습니다.");
          navigate("/my-info");
        } else {
          alert("오류 발생. 다시 시도하세요.");
        }
      }
    } catch (error) {
      console.error("인증 확인 실패:", error);
      alert("인증 코드가 올바르지 않습니다.");
    }
  };

  const handleSavePhone = () => {
    setMemberData({ ...memberData, tel: updatedPhone } as MemberData);
    setIsEditing(false);
    setVerificationCode("");
    setIsVerified(false);
    alert("전화번호가 변경되었습니다.");
  };

  const handleCancelEdit = () => {
    setUpdatedPhone(memberData?.tel || ""); // memberData가 undefined인 경우 기본값 "" 설정
    setVerificationCode("");
    setIsVerified(false);
    setIsEditing(false);
  };

  const handleLogout = async () => {
    const response = await logout();
    let message = "로그아웃에 실패했습니다.";
    if (response.status && response.status == "SUCCESS") {
      removeMember();
      message = "로그아웃 되었습니다.";
    } else {
      if (response.message) {
        message = response.message;
      }
    }
    alert(message);
    navigate("/");
  };

  const handleResetPassword = () => {
    navigate("/my-info/change-password");
  };

  const handleResetPin = () => {
    navigate("/my-info/change-pin");
  };

  const handleDeactivation = async () => {
    const confirmDelete = window.confirm("정말로 탈퇴하시겠습니까?");
    if (confirmDelete) {
      try {
        const response = await deactivation(String(memberId));

        if (response.status === "SUCCESS") {
          alert("계정이 삭제되었습니다.");
          removeMember();
          navigate("/start");
        } else {
          alert("오류가 발생했습니다. 다시 시도해주세요.");
        }
      } catch (error) {
        console.error("탈퇴 요청 실패:", error);
        alert("탈퇴 요청 중 오류가 발생했습니다.");
      }
    }
  };

  return (
    <>
      {isMyinfo ? (
        <div id="app-container" className="min-h-screen flex flex-col p-6">
          {/* 헤더 */}
          <header className="relative flex items-center mb-6">
            <div className="relative group">
              <button className="p-1" onClick={() => navigate("/")}>
                <img src={backIcon} alt="Back" className="w-8 h-8" />
              </button>
              {/* Tooltip */}
              <span className="absolute left-1/2 transform -translate-x-1/2 top-full mt-2 w-max px-3 py-1 text-sm text-white bg-gray-700 rounded opacity-0 group-hover:opacity-100 transition-opacity">
                홈으로 갑니다
              </span>
            </div>
            <h1 className="absolute left-1/2 transform -translate-x-1/2 text-3xl font-bold text-center">
              내 정보
            </h1>
          </header>

          {!isEditing ? (
            <>
              <div>
                <h2 className="text-lg font-bold mb-4">기본정보</h2>
                <div className="space-y-4">
                  <div className="flex justify-between">
                    <p className="text-m text-gray-500">이름</p>
                    <p className="text-m font-medium text-gray-800">
                      {memberData?.name}
                    </p>
                  </div>
                  <div className="flex justify-between">
                    <p className="text-m text-gray-500">휴대전화번호</p>
                    <p className="text-m font-medium text-gray-800">
                      {memberData?.tel && typeof memberData.tel === "string"
                        ? memberData.tel.replace(
                            /^(\d{3})(\d{4})(\d{4})$/,
                            "$1-****-$3"
                          )
                        : "전화번호 없음"}
                    </p>
                  </div>
                  <div className="flex justify-between">
                    <p className="text-m text-gray-500">이메일</p>
                    <p className="text-m font-medium text-gray-800">
                      {memberData?.email}
                    </p>
                  </div>
                  <div className="">
                    <button
                      onClick={() => setIsEditing(true)}
                      className="w-full px-3 py-2 bg-[#536DFE] text-white rounded-lg hover:bg-[#485acf]"
                    >
                      수정하기
                    </button>
                  </div>
                </div>
              </div>

              <div className="mt-6">
                <h2 className="text-lg font-bold mb-4">계정 관리</h2>
                <div className="space-y-4">
                  <div className="flex justify-between">
                    <p className="text-m text-gray-500">비밀번호</p>
                    <button
                      onClick={handleResetPassword}
                      className="px-3 py-2 bg-[#536DFE] text-white rounded-lg hover:bg-[#485acf]"
                    >
                      변경하기
                    </button>
                  </div>
                  {authData?.mydataLinked === "Y" && (
                    <div className="flex justify-between">
                      <p className="text-m text-gray-500">출금 비밀번호</p>
                      <button
                        onClick={handleResetPin}
                        className="px-3 py-2 bg-[#536DFE] text-white rounded-lg hover:bg-[#485acf]"
                      >
                        변경하기
                      </button>
                    </div>
                  )}
                </div>
              </div>

              <div className="mt-6">
                <button
                  onClick={handleDeactivation}
                  className="w-full py-3 bg-red-500 text-white rounded-lg text-sm hover:bg-red-600"
                >
                  탈퇴하기
                </button>
              </div>

              <div className="mt-6">
                <button
                  onClick={handleLogout}
                  className="w-full py-3 bg-gray-300 text-gray-700 rounded-lg hover:bg-gray-400"
                >
                  로그아웃
                </button>
              </div>
            </>
          ) : (
            <>
              <div>
                <h2 className="text-lg font-bold mb-4">정보 수정</h2>
                <div className="space-y-4">
                  <div className="flex justify-between items-center">
                    <p className="text-m text-gray-500">이름</p>
                    <p className="text-m text-gray-600">
                      개명한 경우{" "}
                      <span className="font-medium">관리자에게 문의</span>
                      해주세요.
                    </p>
                  </div>
                  <div className="space-y-2">
                    <label className="text-sm font-medium text-gray-500">
                      휴대전화번호
                    </label>
                    <div className="flex space-x-2">
                      <input
                        type="tel"
                        value={updatedPhone}
                        onChange={(e) => setUpdatedPhone(e.target.value)}
                        className="flex-grow p-2 border border-gray-300 rounded-md"
                        placeholder="01012345678"
                      />
                      <button
                        className="px-3 py-2 bg-[#536DFE] text-white rounded-lg hover:bg-[#485acf]"
                        onClick={handleSendVerification}
                      >
                        인증번호 전송
                      </button>
                    </div>
                    <div className="flex space-x-2">
                      <input
                        type="text"
                        placeholder="인증번호 입력"
                        value={verificationCode}
                        onChange={(e) => setVerificationCode(e.target.value)}
                        className="flex-grow p-2 border border-gray-300 rounded-md"
                      />
                      <button
                        className="px-3 py-2 bg-[#536DFE] text-white rounded-lg hover:bg-[#485acf]"
                        onClick={handleVerifyCode}
                      >
                        인증
                      </button>
                    </div>
                    {isVerified && (
                      <p className="text-sm text-green-500">
                        전화번호 인증이 완료되었습니다.
                      </p>
                    )}
                  </div>
                </div>
              </div>

              <div className="mt-6 flex justify-end space-x-2">
                <button
                  onClick={handleCancelEdit}
                  className="px-4 py-2 bg-gray-300 rounded-lg hover:bg-gray-400"
                >
                  취소
                </button>
                {isVerified && (
                  <button
                    onClick={handleSavePhone}
                    className="px-4 py-2 bg-cyan-500 text-white rounded-lg hover:bg-cyan-600"
                  >
                    저장
                  </button>
                )}
              </div>
            </>
          )}
            {!isEditing && <footer className="mt-auto"><BottomNav /></footer>}
        </div>
      ) : (
        <div id="app-container" className="min-h-screen flex flex-col p-6">
          <Outlet />
        </div>
      )}
    </>
  );
};

export default MyInfo;