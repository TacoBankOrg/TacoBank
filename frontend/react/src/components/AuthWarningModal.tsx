import { useEffect, useState } from "react";

const AuthWarningModal = ({
  isOpen,
  extendSession,
  removeMember,
  remainingTimeInSeconds,
}: {
  isOpen: boolean;
  extendSession: () => Promise<void>;
  removeMember: () => void;
  remainingTimeInSeconds: number;
}) => {
  const [timeLeft, setTimeLeft] = useState(remainingTimeInSeconds);

  useEffect(() => {
    if (!isOpen) return;

    const interval = setInterval(() => {
      setTimeLeft((prevTime) => {
        if (prevTime <= 1) {
          clearInterval(interval); // 시간이 다 되면 카운트다운 종료
          return 0;
        }
        return prevTime - 1;
      });
    }, 1000);

    return () => clearInterval(interval); // 컴포넌트 언마운트 시 정리
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50">
      <div className="bg-white p-6 rounded shadow-lg w-96">
        <h2 className="text-lg font-bold mb-4">세션 연장</h2>
        <p className="mb-6">
          세션이{" "}
          <span className="text-blue-500 font-bold">{timeLeft}초</span> 후
          만료됩니다. 연장하시겠습니까?
        </p>
        <div className="flex justify-between">
          <button
            onClick={removeMember}
            className="bg-red-500 text-white px-4 py-2 rounded w-1/2 mr-2"
          >
            로그아웃
          </button>
          <button
            onClick={extendSession}
            className="bg-[#536DFE] text-white px-4 py-2 rounded w-1/2 ml-2"
          >
            연장하기
          </button>
        </div>
      </div>
    </div>
  );
};

export default AuthWarningModal;