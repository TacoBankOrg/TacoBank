import { tacoCoreApi } from "./tacoApis";
import { ReceiptInfo, ReceiptUploadResponse } from "../types/ocrTypes";
import axios from "axios";

export const uploadReceipt = async (file: File): Promise<ReceiptInfo> => {
  try {
    const formData = new FormData();
    formData.append("file", file); 

    const response = await tacoCoreApi.post<ReceiptUploadResponse>(
      "/core/receipts/ocr", 
      formData,
      {
        headers: {
          "Content-Type": "multipart/form-data", // 파일 전송 시 필요한 헤더
        },
      }
    );

    // response.status가 숫자인 경우
    if (response.status !== 200) { // HTTP 200이 아닌 경우 오류 처리
      throw new Error(`서버 오류: ${response.statusText || "OCR 요청 실패"}`);
    }

    // 성공 여부는 응답 데이터 내 별도 필드로 판단
    if (!response.data || response.data.status !== "SUCCESS") { // data.status가 SUCCESS인지 확인
      throw new Error(response.data.message || "영수증 OCR 인식이 실패했습니다.");
    }

    return response.data.data; 

  } catch (error: unknown) {
    // AxiosError 타입인지 확인
    if (axios.isAxiosError(error)) {
      console.error("Axios error:", error.response?.data || error.message);
      throw new Error(error.response?.data?.message || "영수증 업로드에 실패했습니다. 다시 시도해주세요.");
    } else {
      console.error("Unexpected error:", error);
      throw new Error("예기치 않은 오류가 발생했습니다. 다시 시도해주세요.");
    }
  }
};