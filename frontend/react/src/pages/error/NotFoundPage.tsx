import React from "react";
import { useNavigate } from "react-router-dom";

const NotFoundPage: React.FC = () => {
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
      <h1 style={{ fontSize: "4rem", marginBottom: "1rem" }}>404</h1>
      <p style={{ fontSize: "1.25rem", marginBottom: "2rem" }}>
        페이지를 찾을 수 없습니다.
      </p>
      <button
        onClick={handleGoHome}
        style={{
          backgroundColor: "#f9a825",
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

export default NotFoundPage;