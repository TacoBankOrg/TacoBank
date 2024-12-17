export interface ApiResponse<T> {
  status: "SUCCESS" | "FAILURE" | "TERMINATED";
  message: string;
  data?: T;
}