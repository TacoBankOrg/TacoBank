import React from "react";
import { useNavigate } from "react-router-dom";
import logo from "../assets/image/tacologo.png";

const StartPage: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div id="app-container" className="flex min-h-screen">
      <div className="relative h-screen flex flex-col justify-between">
        <div
          className="absolute top-0 left-0 w-full"
        >
          <div
            className="absolute top-0 right-0 w-full h-full"
          ></div>
          <div
            className="absolute top-0 right-0 w-full h-full"
          ></div>
        </div>

        {/* 로고 및 텍스트 */}
        <div className="text-left ml-8 mt-16 z-10">
          <img src={logo} alt="Logo" className="w-24 h-24" />
          <h1 className="text-6xl font-extrabold text-[#212121] leading-tight">
            ㅌㅋ <br /> ㅂㅋ
          </h1>
        </div>

        {/* 하단 버튼들 */}
        <div className="mb-16 w-full px-8 space-y-4 z-10">
          <button
            onClick={() => navigate("/login")}
            className="w-full py-3 text-white font-semibold bg-[#536DFE] rounded-md flex justify-center items-center space-x-2 hover:bg-[#485acf] transition-colors"
          >
            <span>로그인</span>
            {/* <span>
              <svg
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                strokeWidth={1.5}
                stroke="currentColor"
                className="size-6"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M13.5 4.5 21 12m0 0-7.5 7.5M21 12H3"
                />
              </svg>
            </span> */}
          </button>
          <button
            onClick={() => navigate("/join")}
            className="w-full py-3 text-[#536DFE] font-semibold border border-[#536DFE] hover:bg-[#EAEDFF] rounded-md flex justify-center items-center space-x-2"
          >
            <span>회원가입</span>
            {/* <span>
              <svg
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                strokeWidth={1.5}
                stroke="currentColor"
                className="size-6"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M13.5 4.5 21 12m0 0-7.5 7.5M21 12H3"
                />
              </svg>
            </span> */}
          </button>
        </div>
      </div>
    </div>
  );
};

export default StartPage;