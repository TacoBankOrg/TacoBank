import { useQuery } from "@tanstack/react-query";
import { friendList } from "../api/friendApi";
import { Friends } from "../types/friendTypes";

export const useFriendList = (memberId: number) => {
  return useQuery<Friends[], Error>({
    queryKey: ["friendList", memberId],
    queryFn: async (): Promise<Friends[]> => {
      const response = await friendList(memberId);
      console.log(response)

      if (!response.data || response.status !== "SUCCESS") {
        throw new Error(response.message || "친구 목록 조회 실패");
      }
      
      return response.data as Friends[];
    },
  });
};