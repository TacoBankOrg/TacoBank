import { useQuery, UseQueryOptions } from "@tanstack/react-query";
import { getNotifications, readNotifications } from "../api/memberApi";
import useAuth from "./useAuth";
import { Notification } from "../types/memberTypes";
import { useState, useEffect } from "react";

export const useNotification = () => {
  const { member, authData } = useAuth();
  const memberId = member?.memberId;
  const [unreadNotificationCount, setUnreadNotificationCount] = useState<number>(0);

  // 알림 가져오기
  const {
    data: notifications = [],
    isLoading,
    refetch,
  } = useQuery<Notification[], Error>({
    queryKey: ["notifications", memberId],
    queryFn: async (): Promise<Notification[]> => {
      if (!memberId || authData?.mydataLinked !== "Y") return []; // 계좌 연결 성공 이후에만 알림 로직 실행
      const response = await getNotifications(memberId);
      return response;
    },
    onSuccess: (data: Notification[]) => {
      // 읽지 않은 알림 처리
      const unreadCount = data.filter((notification) => !notification.notificationId).length;
      setUnreadNotificationCount(unreadCount); // 읽지 않은 알림 갯수 업데이트
    },
    enabled: !!memberId && authData?.mydataLinked === "Y", // 계좌 연결 성공 이후에만 활성화
  } as UseQueryOptions<Notification[], Error>);

  // 알림 읽음 처리
  const markAsRead = async (notificationId: number) => {
    try {
      const response = await readNotifications(notificationId); // 서버에서 읽음 처리
      if (response.status === "SUCCESS") {
        // 읽음 처리 성공 시 카운트 감소
        setUnreadNotificationCount((prevCount) => Math.max(prevCount - 1, 0));
      }
      refetch(); // 알림 데이터를 갱신
    } catch (error) {
      console.error("Failed to mark as read:", error);
    }
  };

  // 계좌 연결 성공 여부에 따라 알림 로직 초기화
  useEffect(() => {
    if (authData?.mydataLinked === "Y" && memberId) {
      refetch(); // 계좌 연결 성공 후 알림 데이터를 새로고침
    }
  }, [authData?.mydataLinked, memberId, refetch]);

  return { notifications, isLoading, markAsRead, unreadNotificationCount };
};