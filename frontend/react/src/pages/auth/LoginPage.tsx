import { useState, FormEvent, useRef } from "react";
import backIcon from "../../assets/image/icon/backicon.png";
import { useNavigate } from "react-router-dom";
import EyeOpenIcon from "../../assets/image/icon/EyeOpenIcon";
import EyeClosedIcon from "../../assets/image/icon/EyeClosedIcon";
import { useMutation } from "@tanstack/react-query";
import { login } from "../../api/authApi";
import useAuth from "../../hooks/useAuth";

function LoginPage() {
  const navigate = useNavigate();
  const { startTimer, setIsLoggedIn, updateAuthData } = useAuth();
  const [members, setMembers] = useState({
    password: "",
    email: "",
  });
  const [showPassword, setShowPassword] = useState(false);
  const loginMutation = useMutation({
    mutationFn: login,
    onSuccess: (response) => {
      // 응답 전체 객체
      const { status, message, data } = response; // 응답 구조 해체
      if (status === "SUCCESS") {
        // 성공 상태와 memberId 확인
        const { memberId, mydataLinked } = data;
        alert("로그인 성공!");
        sessionStorage.setItem("isLoggedIn", "true");
        updateAuthData({ memberId, mydataLinked });
        sessionStorage.setItem(
          "authData",
          JSON.stringify({ memberId, mydataLinked })
        );
        console.log(memberId);
        setIsLoggedIn(true);
        startTimer();
        console.log("로그인 성공: 홈으로 이동");
        // 홈으로 이동 전에 잠시 기다리기 (setTimeout)
        setTimeout(() => {
          navigate("/"); // 홈으로 이동
        }, 10);
      } else {
        console.error("로그인 응답에 문제가 있습니다:", message);
      }
    },
    onError: (error) => {
      console.error("로그인 실패:", error);
    },
  });

  const passwordInputRef = useRef<HTMLInputElement>(null);

  // 비밀번호 보기/숨기기 토글 함수
  const togglePasswordVisibility = (event: React.MouseEvent) => {
    event.preventDefault();
    setShowPassword(!showPassword);
    if (passwordInputRef.current) {
      const cursorPosition = passwordInputRef.current.selectionStart;
      setTimeout(() => {
        passwordInputRef.current?.focus();
        passwordInputRef.current?.setSelectionRange(
          cursorPosition,
          cursorPosition
        );
      }, 0); // 커서 위치 유지
    }
  };

  const handleSubmit = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    loginMutation.mutate(members);
  };

  return (
    <div id="app-container" className="flex min-h-screen p-6">
      <header className="relative flex items-center justify-center mb-6">
        <button className="absolute left-0 p-1" onClick={() => navigate(-1)}>
          <img src={backIcon} alt="Back" className="w-8 h-8" />
        </button>
        <h1 className="text-3xl font-bold mx-auto">로그인</h1>
      </header>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <input
            id="email"
            type="text"
            name="email"
            value={members.email}
            onChange={(e) => setMembers({ ...members, email: e.target.value })}
            required
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:border-[#536DFE] focus:ring-0"
            placeholder="이메일"
          />
        </div>

        <div className="relative">
          <div className="relative items-center border border-gray-300 rounded-md focus:outline-none focus-within:border-[#536DFE]">
            <input
              id="password"
              ref={passwordInputRef}
              type={showPassword ? "text" : "password"}
              name="password"
              value={members.password}
              onChange={(e) =>
                setMembers({ ...members, password: e.target.value })
              }
              required
              className="flex-grow px-3 py-2 outline-none rounded-md"
              placeholder="비밀번호(8자리 이상)"
            />
            <button
              type="button"
              className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500 hover:text-black"
              onClick={togglePasswordVisibility}
            >
              {showPassword ? <EyeOpenIcon /> : <EyeClosedIcon />}
            </button>
          </div>
        </div>

        <div className="flex items-center justify-end">
          <div className="text-sm space-x-2 ml-4">
            <a
              onClick={() =>
                navigate("/find-email", { state: { tab: "email" } })
              }
              className="hover:underline"
            >
              이메일 찾기
            </a>
            <span>|</span>
            <a
              onClick={() =>
                navigate("/find-password", { state: { tab: "password" } })
              }
              className="hover:underline"
            >
              비밀번호 찾기
            </a>
          </div>
        </div>

        <button
          type="submit"
          className={`w-full py-2 rounded-md transition-colors ${
            members.email && members.password
              ? "bg-[#536DFE] text-white hover:bg-[#485acf]"
              : "bg-gray-300 text-gray-500 cursor-not-allowed"
          }`}
          disabled={!members.email || !members.password}
        >
          로그인
        </button>
      </form>

      <footer className="mt-6 text-center text-sm text-gray-600">
        계정이 없으신가요?{" "}
        <a href="/join" className="text-[#536DFE] hover:underline">
          회원가입
        </a>
      </footer>
    </div>
  );
}

export default LoginPage;
