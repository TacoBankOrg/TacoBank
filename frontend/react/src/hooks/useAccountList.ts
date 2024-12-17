import { useQuery } from "@tanstack/react-query";
import { getAccountList } from "../api/accountApi";
import { Account } from "../types/accountTypes";
export const useAccountList = (memberId: number) => {
  return useQuery<Account[], Error>({
    queryKey: ["accountList", memberId],
    queryFn: async (): Promise<Account[]> => {
      const response = await getAccountList(memberId);
      
      console.log(response)
      if (response.status !== "SUCCESS") {
        throw new Error(response.message || "계좌 조회 실패");
      }
      return response.data;
    },
  });
};
