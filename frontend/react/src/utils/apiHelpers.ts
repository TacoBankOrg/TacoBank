import { AxiosError } from "axios";
import { ApiResponse } from "../types/commonTypes";

export const handleApiResponse = <T>(
  response: ApiResponse<T | { data: T }>
): T => {
  switch (response.status) {
    case "SUCCESS":
      if (response.data !== undefined) {
        if (
          typeof response.data === "object" &&
          response.data !== null &&
          "data" in response.data
        ) {
          return (response.data as { data: T }).data; // 중첩 데이터 반환
        }
        return response.data; // 평범한 데이터 반환
      }
      return {} as T; // 데이터가 없을 경우 빈 값 반환 (필요 시 수정 가능)
    case "FAILURE":
      throw new Error(response.message || "요청 실패");
    case "TERMINATED":
      throw new Error(response.message || "요청이 중단되었습니다");
    default:
      throw new Error("알 수 없는 응답 상태");
  }
};

export const handleApiError = (
  error: AxiosError<ApiResponse<unknown>>
): string => {
  if (error.response && error.response.data) {
    const response = error.response.data;

    switch (response.status) {
      case "FAILURE":
        return response.message || "작업이 실패했습니다. 다시 시도하세요.";
      case "TERMINATED":
        return response.message || "작업이 종료되었습니다.";
      default:
        return "서버에서 알 수 없는 응답을 받았습니다.";
    }
  }

  // 네트워크 오류 또는 기타 예외
  return error.message || "네트워크 오류가 발생했습니다. 인터넷 연결을 확인해주세요.";
};
