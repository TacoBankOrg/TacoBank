import { useQuery } from "@tanstack/react-query";
import { Group } from "../types/groupTypes";
import { groupList } from "../api/groupApi";
export const useGroupList = (memberId: number) => {
    return useQuery<Group[], Error>({
        queryKey: ["groupList", memberId],
        queryFn: async (): Promise<Group[]> => {
            const response = await groupList(memberId); // API 호출

            // console.log(response)
            if (response.status !== "SUCCESS") {
                throw new Error(response.message || "그룹 조회 실패");
            }
            return response.data as Group[]; // 전체 응답 반환
        },
    });
};