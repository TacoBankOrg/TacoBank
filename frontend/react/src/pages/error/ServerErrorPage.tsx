import React from "react";
import { useNavigate } from "react-router-dom";

const ServerErrorPage: React.FC = () => {
  const navigate = useNavigate();

  const handleGoHome = () => {
    navigate("/");
  };

  return (
    <div
      style={{
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        height: "100vh",
        textAlign: "center",
      }}
    >
      <h1 style={{ fontSize: "4rem", marginBottom: "1rem" }}>500</h1>
      <p style={{ fontSize: "1.1rem", marginBottom: "2rem" }}>
        서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.
      </p>
      <button
        onClick={handleGoHome}
        style={{
          backgroundColor: "#d32f2f",
          color: "white",
          border: "none",
          padding: "0.75rem 1.5rem",
          fontSize: "1rem",
          borderRadius: "5px",
          cursor: "pointer",
        }}
      >
        홈으로 돌아가기
      </button>
    </div>
  );
};

export default ServerErrorPage;