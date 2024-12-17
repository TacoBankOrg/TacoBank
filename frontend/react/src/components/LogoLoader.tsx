import React from "react";
import loading from "../assets/image/tacologo.png";

const LogoLoader: React.FC = () => {
  return (
    <div className="flex flex-col justify-center items-center h-full mt-40">
      <img
        src={loading}
        alt="loading"
        className="w-[120px] h-[120px] animate-grayscaleToColor"
      />
      <p className="text-gray-500 text-lg">Loading...</p>
    </div>
  );
};

export default LogoLoader;