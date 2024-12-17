import axios from "axios";
import { AuthContextType } from "../context/AuthContext";
import { handleApiError } from "../utils/apiHelpers";

// Auth API 인스턴스
export const tacoAuthApi = axios.create({
  baseURL: import.meta.env.VITE_API_URL_AUTH,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true, // 쿠키 포함
});

// Core API 인스턴스
export const tacoCoreApi = axios.create({
  baseURL: import.meta.env.VITE_API_URL_CORE,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true, // 쿠키 포함
});

const activeRequests = new Set<string>();
const handledErrors = new Set<string>(); // 오류를 처리한 URL을 기록하여 중복 alert 방지

// 인터셉터를 공통으로 추가
const setupInterceptors = (
  apiInstance: typeof tacoAuthApi | typeof tacoCoreApi,
  removeMember: AuthContextType["removeMember"]
) => {
  apiInstance.interceptors.request.use((config) => {
    if (config.url) {
      activeRequests.add(config.url); // 요청 시작 시 URL 추가

      // 로그인 요청인 경우 handledErrors에서 URL 제거 (alert 다시 뜨도록 초기화)
      if (config.url.includes("/auth/login")) {
        handledErrors.delete(config.url);
      }
    }
    return config;
  });


  apiInstance.interceptors.response.use(
    (response) => {
      if (response.config.url) {
        activeRequests.delete(response.config.url); // 요청 성공 시 URL 제거
      }
      return response;
    },
    (error) => {
      const url = error.config?.url;
      if (url) {
        activeRequests.delete(url); // 요청 실패 시 URL 제거
      }

      const statusCode = error.response?.status;
      const errorMessage =
        error.response?.data?.message || "알 수 없는 오류가 발생했습니다.";

      // 401 에러 처리
      if (statusCode === 401 && !handledErrors.has(url)) {
        alert(errorMessage);
        handledErrors.add(url); // 이미 처리한 오류는 다시 처리하지 않음
        if (url && !url.includes("/auth/login")) {
          removeMember(); // 세션 제거 및 스타트 페이지로 리다이렉트
        }
      }
      // 500 에러 처리
      else if (statusCode === 500 && !handledErrors.has(url)) {
        alert(errorMessage);
        handledErrors.add(url); // 이미 처리한 오류는 다시 처리하지 않음

        // 홈으로 리다이렉트
        window.location.href = "/";
      } else {
        handleApiError(error); // 그 외의 에러는 handleApiError로 처리
      }
      return Promise.reject(error); // 에러를 호출자에게 전달
    }
  );
};

// 각 인스턴스에 인터셉터 설정 적용
export const initializeInterceptors = (
  removeMember: AuthContextType["removeMember"]
) => {
  setupInterceptors(tacoAuthApi, removeMember);
  setupInterceptors(tacoCoreApi, removeMember);
};
