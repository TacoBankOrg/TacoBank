import React from "react";
import { Navigate, Outlet } from "react-router-dom";
import useAuth from "../hooks/useAuth";

const PublicRoute: React.FC = () => {
  const { isLoggedIn } = useAuth();

  // 로그인 상태에서 접근 제한 (e.g., 로그인 페이지, 회원가입 페이지)
  if (isLoggedIn) {
    return <Navigate to="/" replace />; // 이미 로그인했으므로 홈으로 리디렉션
  }

  // 비로그인 상태에서 허용
  return <Outlet />;
};

export default PublicRoute;