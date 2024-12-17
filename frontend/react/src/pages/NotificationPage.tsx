import React from "react";
import { useNavigate } from "react-router-dom";
import backIcon from "../assets/image/icon/backicon.png";
import { useNotification } from "../hooks/useNotification";
import { Notification } from "../types/memberTypes";

const NotificationPage: React.FC = () => {
  const navigate = useNavigate();

  // 알림 가져오기
  const { notifications = [], isLoading, markAsRead } = useNotification();

  const calculateDaysLeft = (createdDate: string): number => {
    const created = new Date(createdDate);
    const now = new Date();
    const diffTime =
      created.getTime() + 10 * 24 * 60 * 60 * 1000 - now.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays > 0 ? diffDays : 0; // 0일 이하로는 표시하지 않음
  };

  // 메시지 또는 타입에 따라 이동 경로 설정
  const getRedirectPath = (notification: { message: string }): string => {
    if (notification.message.includes("정산 요청")) {
      return "/settlement/send";
    }
    return "/"; // 기본 경로
  };

  // 알림 클릭 핸들러
  const handleNotificationClick = (notification: Notification) => {
    const redirectPath = getRedirectPath(notification); // 경로 결정
    navigate(redirectPath); // 경로로 이동
    markAsRead(notification.notificationId); // 알림 읽음 처리
  };

  return (
    <div className="relative min-h-screen p-6 text-gray-900" id="app-container">
      <header className="relative flex items-center mb-6">
        <button className="absolute left-0 p-1" onClick={() => navigate("/")}>
          <img src={backIcon} alt="Back" className="w-8 h-8" />
        </button>
        <h1 className="text-3xl font-bold w-full text-center">
          알림
        </h1>
      </header>

      {/* 알림 리스트 */}
      <main className="flex-1 overflow-y-auto">
        {isLoading ? (
          <p className="text-center text-gray-500">알림을 불러오는 중...</p>
        ) : notifications.length === 0 ? (
          <p className="text-center text-gray-500">알림이 없습니다.</p>
        ) : (
          <div className="space-y-4">
            {notifications.map((notification) => (
              <div
                key={notification.notificationId}
                className={`flex flex-col space-y-2 border-b ${notification.is_read ? 'bg-gray-100' : 'bg-white'}`} 
                onClick={() => handleNotificationClick(notification)} // 알림 클릭 시 읽음 처리
              >
                {/* 알림 내용 */}
                <p className="text-sm text-gray-800">{notification.message}</p>
                <div className="flex justify-between items-center text-xs text-gray-500 pb-4">
                  <p>{new Date(notification.createdDate).toLocaleString()}</p>
                  <p className="text-red-500">
                    {calculateDaysLeft(notification.createdDate) > 0
                      ? `${calculateDaysLeft(notification.createdDate)}일 후 삭제됩니다.`
                      : "곧 삭제됩니다."}
                  </p>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
};

export default NotificationPage;