import React from "react";
import { useNavigate } from "react-router-dom";
import logo from "../assets/image/tacologo.png";

const SplashPage: React.FC = () => {
  const navigate = useNavigate();
  const handleGetStarted = () => {
    localStorage.setItem('hasVisitedSplash', 'true');
    navigate('/start');
  };

  return (
    <div id="splash-container" className="flex justify-center items-center min-h-screen">
      <div className="p-6">
        <div className="text-center p-6">
          <img src={logo} alt="logo" className="w-[135px] h-[135px] mx-auto" />
          <div className="font-bold text-5xl">
            ㅌㅋㅂㅋ
          </div>
          <p className="text-md mt-1 mb-20">바로 송금이 가능한 ㅌㅋㅂㅋ</p>
          <p className="mt-4 text-sm">이 화면은 처음 한 번만 보실 수 있습니다.</p>
          <button
            type="button"
            onClick={handleGetStarted}
            className="w-full px-3 py-2 bg-[#536DFE] text-white rounded-md hover:bg-[#485acf] transition-colors font-medium"
          >
            시작하기
          </button>
        </div>
      </div>
    </div>
  );
};

export default SplashPage;