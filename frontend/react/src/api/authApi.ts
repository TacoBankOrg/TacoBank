import { tacoAuthApi } from "./tacoApis";
import { Member } from "../types/memberTypes";
import { handleApiResponse } from "../utils/apiHelpers";

export const join = async (member: Member): Promise<void> => {
  const response = await tacoAuthApi.post(
    "/auth/members",
    member
  );
  return handleApiResponse(response.data);
};

export const login = async (member: Member) => {
  const response = await tacoAuthApi.post("/auth/login", member);
  console.log(response.data);
  return response.data;
};

export const checkEmailDuplicate = async (email: string) => {
    const response = await tacoAuthApi.post("/auth/email", { email });
    console.log(response)
    return handleApiResponse(response.data);
};

//로그아웃
export const logout = async () => {
  const response = await tacoAuthApi.post(`/auth/logout`);
  return response.data;
};
