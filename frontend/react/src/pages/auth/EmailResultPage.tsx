import { useLocation, useNavigate } from "react-router-dom";

export default function EmailResult() {
  const navigate = useNavigate();
  const { state } = useLocation(); // 이메일 데이터 받기
  const email = state?.email || "이메일 정보를 찾을 수 없습니다.";

  return (
    <div id="app-container" className="flex min-h-screen p-6">
      {/* 상단 정보 */}
      <div className="p-6 w-full max-w-md text-center">
        <div className="mb-4">
          <div className="flex justify-center items-center w-16 h-16 bg-[#536DFE] rounded-full mx-auto">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              className="h-8 w-8 text-white"
              viewBox="0 0 20 20"
              fill="currentColor"
            >
              <path
                fillRule="evenodd"
                d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-8.707a1 1 0 00-1.414-1.414L9 11.586 7.707 10.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                clipRule="evenodd"
              />
            </svg>
          </div>
          <h1 className="text-2xl font-bold text-gray-800 mt-4">
            이메일 찾기 완료
          </h1>
        </div>
        <div className="text-sm text-gray-600">
          <p className="mt-2 font-bold text-blue-600">{email}</p>
        </div>
      </div>

      {/* 하단 버튼 */}
      <div className="mt-8 w-full max-w-md space-y-3">
        <button
          onClick={() => navigate("/find-password")}
          className="w-full py-3 text-white font-semibold bg-[#536DFE] rounded-md flex justify-center items-center space-x-2 hover:bg-[#485acf] transition-colors"
        >
          비밀번호 찾기
        </button>
        <button
          onClick={() => navigate("/login")}
          className="w-full py-3 text-[#536DFE] font-semibold border border-[#536DFE] hover:bg-[#EAEDFF] rounded-md flex justify-center items-center space-x-2"
        >
          로그인 하러가기
        </button>
      </div>
    </div>
  );
}
