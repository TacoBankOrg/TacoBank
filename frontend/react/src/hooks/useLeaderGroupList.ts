import { useQuery } from "@tanstack/react-query";
import { Group } from "../types/groupTypes";
import { leaderGroupList } from "../api/groupApi";
export const useLeaderGroupList = (memberId: number) => {
  return useQuery<Group[], Error>({
    queryKey: ["leaderGroupList", memberId],
    queryFn: async (): Promise<Group[]> => {
      const response = await leaderGroupList(memberId); // API 호출
      
      // console.log(response)
      if (response.status !== "SUCCESS") {
        throw new Error(response.message || "그룹 조회 실패");
      }
      return response.data as Group[]; // 전체 응답 반환
    },
  });
};