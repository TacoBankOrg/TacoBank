import React, { useEffect, useState, ReactNode, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { Member } from "../types/memberTypes";
import AuthWarningModal from "../components/AuthWarningModal";
import { tacoAuthApi } from "../api/tacoApis";

export interface AuthContextType {
  member: Member | null;
  startTimer: () => void;
  setMember: (member: Member) => void;
  isLoggedIn: boolean;
  setIsLoggedIn: (value: boolean) => void;
  removeMember: () => void;
  authData: { memberId: number; mydataLinked: string } | null; // authData 추가
  updateAuthData: (authData: { memberId: number; mydataLinked: string }) => void; // Setter 추가
  amount: string | null; // 금액
  setAmount: (amount: string) => void; // 금액 설정
  clearAmount: () => void;
}

export const AuthContext = React.createContext<AuthContextType>({
  member: null,
  startTimer: () => {},
  setMember: () => {},
  removeMember: () => {},
  isLoggedIn: false,
  setIsLoggedIn: () => {},
  authData: null, // 초기값
  updateAuthData: () => {}, // 초기값
  amount: null, // 초기값
  setAmount: () => {}, // 초기값
  clearAmount: () => {}, // 초기값
});

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const navigate = useNavigate();
  const [amount, setAmountState] = useState<string | null>(() => {
    return sessionStorage.getItem("amount") || null;
  });

  const setAmount = (newAmount: string) => {
    setAmountState(newAmount);
    sessionStorage.setItem("amount", newAmount);
  };

  const clearAmount = () => {
    setAmountState(null);
    sessionStorage.removeItem("amount");
  };

  const [isLoggedIn, setIsLoggedInState] = useState<boolean>(() => {
    return sessionStorage.getItem("isLoggedIn") === "true";
  });

  const [authData, setAuthData] = useState<{ memberId: number; mydataLinked: string } | null>(() => {
    const storedAuthData = sessionStorage.getItem("authData");
    return storedAuthData ? JSON.parse(storedAuthData) : null;
  });
  const [isWarningModalVisible, setIsWarningModalVisible] = useState(false);
  // 멤버 정보 관리
  const [member, setMemberState] = useState<Member | null>(() => {
    const storedMember = sessionStorage.getItem("memberData");
    return storedMember ? JSON.parse(storedMember) : null;
  });

  const logoutTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const warningTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const setIsLoggedIn = (value: boolean) => {
    setIsLoggedInState(value);
    sessionStorage.setItem("isLoggedIn", value.toString());
  };

  const setMember = (member: Member) => {
    setMemberState(member);
    sessionStorage.setItem("memberData", JSON.stringify(member));
  };

  const updateAuthData = (newAuthData: { memberId: number; mydataLinked: string }) => {
    setAuthData(newAuthData);
    sessionStorage.setItem("authData", JSON.stringify(newAuthData));
  };


  // 로그아웃 처리 함수
  const removeMember = () => {
    if (logoutTimerRef.current) clearTimeout(logoutTimerRef.current); // 로그아웃 타이머 해제
    if (warningTimerRef.current) clearTimeout(warningTimerRef.current); // 경고 타이머 해제
    setIsWarningModalVisible(false);
    setMemberState(null);
    setIsLoggedInState(false); // 로그인 상태 false로 설정
    sessionStorage.clear();
    sessionStorage.removeItem("isLoggedIn"); // 로컬스토리지에서 로그인 상태 제거
    sessionStorage.removeItem("memberData");
    sessionStorage.removeItem("authData");
    navigate("/", { replace: true });
  };

  // 타이머 상수 추가
  const WARNING_TIME = 8 * 60 * 1000 + 55 * 1000; // 경고 시간 (8분 55초)
  const LOGOUT_TIME = 9 * 60 * 1000 + 55 * 1000; // 로그아웃 시간 (9분 55초)
  
  // 타이머 시작 함수
  const startTimer = () => {
    const startTime = Date.now(); // 타이머 시작 시간 저장
    sessionStorage.setItem("timerStartTime", startTime.toString());
    resetTimers(0); // 초기 타이머 설정
  };

  // 타이머 재설정 함수
  const resetTimers = (elapsedTime: number) => {
    if (logoutTimerRef.current) clearTimeout(logoutTimerRef.current);
    if (warningTimerRef.current) clearTimeout(warningTimerRef.current);

    const remainingWarningTime = WARNING_TIME - elapsedTime;
    const remainingLogoutTime = LOGOUT_TIME - elapsedTime;

    if (remainingWarningTime > 0) {
      warningTimerRef.current = setTimeout(() => {
        setIsWarningModalVisible(true); // 경고 모달 표시
      }, remainingWarningTime);
    }

    if (remainingLogoutTime > 0) {
      logoutTimerRef.current = setTimeout(() => {
        alert("인증 시간이 만료되었습니다. 다시 로그인해주세요.");
        removeMember(); // 로그아웃 처리
      }, remainingLogoutTime);
    }
  };

  // 타이머 복구 로직 추가
  useEffect(() => {
    const savedStartTime = sessionStorage.getItem("timerStartTime");

    if (savedStartTime) {
      const elapsedTime = Date.now() - parseInt(savedStartTime, 10);

      if (elapsedTime >= LOGOUT_TIME) {
        alert("인증 시간이 만료되었습니다. 다시 로그인해주세요.");
        removeMember(); // 인증 만료 처리
      } else {
        resetTimers(elapsedTime); // 남은 시간으로 타이머 재설정
        if (elapsedTime >= WARNING_TIME) {
          setIsWarningModalVisible(true); // 경고 모달 표시
        }
      }
    }
  }, [removeMember]);

  // 세션 연장
  const extendSession = async () => {
      try {
      const response = await tacoAuthApi.post("/auth/extend-session"); // 백엔드 API 호출
      console.log(response)

      if (response.data && response.data.status === "SUCCESS") {
        alert("성공적으로 연장되었습니다!");
        setIsWarningModalVisible(false); // 모달 닫기

        if (logoutTimerRef.current) clearTimeout(logoutTimerRef.current);
        if (warningTimerRef.current) clearTimeout(warningTimerRef.current);
        setIsWarningModalVisible(false); // 모달 숨김
        startTimer(); // 새로운 타이머 시작

      } else {
        throw new Error("세션 연장 실패");
      }
    } catch (error) {
      console.error("세션 연장 중 오류 발생:", error);
      alert("세션 연장 중 오류가 발생했습니다. 다시 시도해주세요.");
    }
  };


  // 로컬 스토리지에서 isLoggedIn이 변경될 때마다 상태를 확인하고 처리
  useEffect(() => {
    const handleStorageChange = () => {
      const storedIsLoggedIn = sessionStorage.getItem("isLoggedIn") === "true";
      if (!storedIsLoggedIn) {
        removeMember(); // 로그아웃 처리
      }
    };
    // storage 이벤트 감지
    window.addEventListener("storage", handleStorageChange);

    return () => {
      window.removeEventListener("storage", handleStorageChange);
    };
  }, []);

  useEffect(() => {
    const storedMember = sessionStorage.getItem("memberData");
    const storedIsLoggedIn = sessionStorage.getItem("isLoggedIn") === "true";
    const storedAuthData = sessionStorage.getItem("authData")

    if (storedMember) {
      setMemberState(JSON.parse(storedMember));
    }

    if (storedAuthData) {
      setAuthData(JSON.parse(storedAuthData))
    }

    setIsLoggedInState(storedIsLoggedIn);
  }, []);

  return (
    <AuthContext.Provider
      value={{
        member,
        setMember,
        startTimer,
        removeMember,
        isLoggedIn,
        setIsLoggedIn,
        authData,
        updateAuthData,
        amount,
        setAmount,
        clearAmount,
      }}
    >
      {children}
      {isWarningModalVisible && (
        <AuthWarningModal
          isOpen={isWarningModalVisible}
          extendSession={extendSession}
          removeMember={removeMember}
          remainingTimeInSeconds={60} // 초기 남은 시간 설정
        />
      )}
    </AuthContext.Provider>
  );
};
