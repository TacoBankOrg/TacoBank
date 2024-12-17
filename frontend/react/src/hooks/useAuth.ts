import { useContext } from "react";
import { AuthContext } from "../context/AuthContext";

export default function useAuth() {
  const context = useContext(AuthContext);
  if (context) {
    return context;
  }
  throw new Error("프로바이더 이슈");
}