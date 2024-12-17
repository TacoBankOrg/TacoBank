import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import BottomNav from "../components/BottomNav";
import SMSVerificationModal from "../components/SMSVerificationModal";
import PinCreationModal from "../components/PinCreateModal";
import { Member } from "../types/memberTypes";
import { Account, HomeRsp, MainAccount, Transaction } from "../types/homeTypes";
import { requestHome } from "../api/homeApi";
import { useNotification } from "../hooks/useNotification";
import MainAccountIcon from "../assets/image/icon/MainAccountIcon";
import { mainAccountEdit, mainAccountSelect } from "../api/accountApi";
import LogoLoader from "../components/LogoLoader";
import useAuth from "../hooks/useAuth";

interface HomeData extends Member {
  accounts: (Account & { transactionList: Transaction[] })[]; // 계좌 및 거래 내역
  isPinCreated: boolean; // PIN 생성 상태
}

const HomePage: React.FC = () => {
  const { authData, member, setMember, removeMember, updateAuthData } =
    useAuth();
  const navigate = useNavigate();
  const [homeData, setHomeData] = useState<HomeData | null>(null);
  const [isSmsModalOpen, setIsSmsModalOpen] = useState(false); // SMS 인증 모달 상태
  const [isPinModalOpen, setIsPinModalOpen] = useState(false); // PIN 생성 모달 상태
  const [isResetPin, setIsResetPin] = useState(false); // 핀 등록 목적 플래그
  const [expandedAccountId, setExpandedAccountId] = useState<number | null>(
    null
  ); // 확장된 계좌 ID
  const maxVisibleAccounts = 3; // 기본 표시할 계좌 수
  const [showAllAccounts, setShowAllAccounts] = useState(false);
  const { unreadNotificationCount } = useNotification(); // 알림 데이터 가져오기
  const [isLoading, setIsLoading] = useState(true);
  const [isDataFetched, setIsDataFetched] = useState(false); // fetchData 중복실행 방지 플래그

  useEffect(() => {
    if (authData?.mydataLinked === "Y" && !homeData && authData !== null) {
      fetchData(); // 계좌 연결 데이터 로드
    } else {
      setIsSmsModalOpen(false);
    }
  }, [authData]); // authData 변경시 실행

  const fetchData = async () => {
    if (isDataFetched) return; // 중복 실행 방지
    setIsDataFetched(true); // 데이터 가져오기 시작 표시

    const minimumLoadingTime = new Promise((resolve) =>
      setTimeout(resolve, 1000)
    );
    try {
      setIsLoading(true);
      const response = await requestHome();
      console.log(response);
      const data: HomeRsp = response;
      const transformedData: HomeData = {
        name: data.name,
        email: data.email,
        isPinCreated: data.pinSet,
        mydataLinked: data.mydataLinked,
        mainAccountId: data.mainAccountId,
        accounts: data.accountList.map((account) => ({
          ...account,
          transactionList: account.transactionList || [],
        })),
        password: "",
      };
      const memberData = {
        memberId: data.memberId,
        name: data.name,
        email: data.email,
        tel: data.tel,
        mainAccountId: data.mainAccountId,
      };
      setMember(memberData);
      sessionStorage.setItem("memberData", JSON.stringify(memberData));
      setHomeData(transformedData);

      if (authData?.mydataLinked !== data.mydataLinked) {
        updateAuthData({
          memberId: data.memberId,
          mydataLinked: data.mydataLinked,
        });
      }

      if (!data.pinSet) {
        alert(
          "출금 비밀번호가 설정되어있지 않아, 출금 비밀번호 설정을 시작합니다"
        );
        setIsSmsModalOpen(true); // 문자 인증 모달
      }

      await minimumLoadingTime;
    } catch (error) {
      console.error("API 요청 실패", error);
    } finally {
      // 로딩 종료
      setIsLoading(false);
    }
  };

  const handleScrollToTop = () => {
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleSmsVerifySuccess = () => {
    setIsSmsModalOpen(false); // SMS 모달 닫기
    setIsPinModalOpen(true); // PIN 모달 열기

    // 플래그 설정
    if (authData?.mydataLinked === "Y") {
      setIsResetPin(true); // 핀 초기화 목적으로 설정
    } else {
      setIsResetPin(false); // 최초 계좌 연동 목적으로 설정
    }
  };

  const handleAccountLinkClick = () => {
    setIsSmsModalOpen(true); // 컨텍스트에 member가 없으면 SMS 인증
  };

  const handlePinClose = () => {
    if (!isResetPin) {
      // 최초 설정일 때만 컨펌창 표시
      const userConfirmation = window.confirm(
        "서비스 이용이 불가능합니다. 로그아웃 하시겠습니까?"
      );
      if (userConfirmation) {
        // "네"를 눌렀을 경우
        removeMember(); // 로그아웃 함수 실행
      } else {
        // "아니요"를 눌렀을 경우
        console.log("사용자가 로그아웃을 취소했습니다."); // 로그만 출력, 추가 동작 없음
      }
    } else {
      // 초기화일 때는 단순히 PIN 모달 닫기만 수행
      console.log("PIN 초기화 작업이 취소되었습니다.");
      setIsPinModalOpen(false); // 모달 닫기
    }
  };

  const handlePinCreated = async () => {
    setIsPinModalOpen(false);

    if (isResetPin) {
      // 핀 초기화 시
      // alert("PIN이 성공적으로 초기화되었습니다.");
    } else {
      // 최초 계좌 연동 시
      fetchData(); // 데이터를 다시 로드
      alert(
        "계좌 연동이 완료되었습니다! 메인 계좌를 잔액이 가장 많은 계좌로 설정하였으니, 변경을 원하신다면 변경할 계좌의 옆에 별을 눌러서 설정해주세요!"
      );
    }
  };

  const formatDate = (dateTime: string): { date: string; time: string } => {
    const year = dateTime.slice(0, 4); // yy
    const month = dateTime.slice(4, 6); // mm
    const day = dateTime.slice(6, 8); // dd
    const hour = dateTime.slice(9, 11); // HH
    const minute = dateTime.slice(11, 13); // MM
    const second = dateTime.slice(13, 15);

    return {
      date: `${year}.${month}.${day}`, // yymmdd 형식
      time: `${hour}:${minute}:${second}`, // 시간과 분
    };
  };

  const toggleAccountExpansion = (accountId: number) => {
    setExpandedAccountId((prevId) => (prevId === accountId ? null : accountId));
  };

  const toggleMainAccount = async (accountId: number) => {
    if (!homeData || !member) return;
    const currentMainAccountId = member.mainAccountId;
    // 새로 선택된 계좌가 기존 메인 계좌와 동일하면 경고 메시지 출력 후 종료
    if (currentMainAccountId === accountId) {
      alert("이미 선택된 메인 계좌입니다.");
      return;
    }

    try {
      const dto: MainAccount = {
        memberId: member.memberId as number,
        accountId,
      };

      // 서버에 업데이트 요청
      console.log(dto);
      let success = false;
      if (currentMainAccountId === null) {
        // 메인 계좌가 없으면 새로운 메인 계좌 설정
        const selectResponse = await mainAccountSelect(dto);
        if (selectResponse.status === "SUCCESS") {
          success = true;
        } else {
          console.error(selectResponse.message);
          alert(
            selectResponse.message || "메인 계좌 설정 중 문제가 발생했습니다."
          );
        }
      } else {
        // 메인 계좌가 있으면 기존 메인 계좌를 수정
        const editResponse = await mainAccountEdit(dto);
        if (editResponse.status === "SUCCESS") {
          success = true;
        } else {
          console.error(editResponse.message);
          alert(
            editResponse.message || "메인 계좌 수정 중 문제가 발생했습니다."
          );
        }
      }

      if (!success) return;

      // UI 상태 업데이트
      setMember({
        ...member,
        mainAccountId: accountId,
      });

      setHomeData((prev) => {
        if (!prev) return null;

        const updatedAccounts = prev.accounts.map((account) => ({
          ...account,
          mainAccountId: account.accountId === accountId,
        }));

        return {
          ...prev,
          accounts: updatedAccounts,
        };
      });

      alert("메인 계좌가 성공적으로 변경되었습니다.");
    } catch (error) {
      console.error("API 요청 실패", error);
    }
  };

  const renderAccounts = () => {
    if (!homeData || !homeData.accounts || homeData.accounts.length === 0) {
      return null; // 계좌 데이터가 없으면 아무것도 렌더링하지 않음
    }

    // 메인 계좌를 맨 앞에 오도록 정렬
    const sortedAccounts = [...homeData.accounts].sort((a, b) => {
      if (a.accountId === member?.mainAccountId) return -1; // 메인 계좌는 가장 앞
      if (b.accountId === member?.mainAccountId) return 1;
      return 0; // 나머지는 순서 유지
    });
    const accountsToShow = showAllAccounts
      ? sortedAccounts
      : sortedAccounts.slice(0, maxVisibleAccounts);
    const groupByDate = (transactions: Transaction[]) => {
      return transactions.reduce((acc, transaction) => {
        const { date } = formatDate(transaction.tranDateTime);
        if (!acc[date]) {
          acc[date] = [];
        }
        acc[date].push(transaction);
        return acc;
      }, {} as Record<string, Transaction[]>);
    };

    return (
      <>
        {accountsToShow.map((account) => {
            const isMainAccount = account.accountId === member?.mainAccountId;
            const cardBgClass = isMainAccount ? "bg-[#536DFE]" : "bg-[#EAEDFF]";
            const cardTextClass = isMainAccount ? "text-[#CFD6FC]" : "text-[#757575]";
            const cardTranClass = isMainAccount ? "text-white" : "text-[#424242]";
            
          const groupedTransactions = groupByDate(
            account.transactionList || []
          );
          return (
            <div
              key={account.accountId}
              className={`flex flex-col ${cardBgClass} ${cardTextClass} rounded-lg text-md mb-4 p-3 relative`}
              onClick={() => navigate(`/account/detail/${account.accountId}`)} // 클릭 시 이동
            >
              <div className="flex items-center justify-between">
                <div>
                  <p
                    className={`text-xs cursor-pointer underline ${cardTextClass}`}
                    onClick={(e) => {
                      e.stopPropagation();
                      const textToCopy = `${account.bankName} ${account.accountNum}`;
                      navigator.clipboard
                        .writeText(textToCopy)
                        .then(() => {
                          alert("계좌 번호가 복사되었습니다!");
                        })
                        .catch(() => {
                          alert("복사에 실패했습니다.");
                        });
                    }}
                  >
                    {account.bankName} {account.accountNum}
                  </p>
                  <p className={`${cardTranClass} font-bold text-gray-800 text-lg`}>
                    {account.balance.toLocaleString()}원
                  </p>
                  <p className="text-sm font-medium">{account.accountName}</p>
                </div>
                <button
                  className="absolute top-3 right-3"
                  onClick={(e) => {
                    e.stopPropagation();
                    toggleMainAccount(account.accountId);
                  }}
                >
                  <MainAccountIcon
                    filled={account.accountId === member?.mainAccountId}
                    size={24}
                  />
                </button>
              </div>
              <div className="flex justify-end mt-2">
                <button
                  className={`text-sm mr-auto ${
                    account.balance === 0
                      ? "bg-[#E6E6E6] text-white" // 스타일 유지
                      : "bg-[#E6E6E6] text-white"
                  } px-3 py-1 rounded-xl`}
                  style={{
                    color: isMainAccount ? "#536DFE" : "#6B7280", // 메인 계좌는 배경색, 그 외는 회색
                  }}
                  onClick={(e) => {
                    e.stopPropagation();
                    if (account.balance === 0) {
                      alert("잔액이 없어 송금이 불가능합니다."); // Alert 메시지
                      return; // 로직 차단
                    }
                    navigate("/transfer", {
                      state: {
                        banksCode: account.bankCode,
                        accountName: account.accountHolder,
                        accountId: account.accountId,
                      },
                    });
                  }}
                >
                  송금
                </button>
                <button
                  className="flex items-center text-sm text-gray-800"
                  onClick={(e) => {
                    e.stopPropagation(); // 이벤트 전파 중지
                    toggleAccountExpansion(account.accountId);
                  }}
                >
                  {expandedAccountId === account.accountId ? (
                    <>
                      <span className={`${cardTextClass}`}>닫기</span>
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        fill="none"
                        viewBox="0 0 24 24"
                        strokeWidth={1.5}
                        stroke="currentColor"
                        className="w-4 h-4"
                        style={{ stroke: isMainAccount ? "#FFFFFF" : "#6B7280" }}
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          d="M6 15l6-6 6 6"
                        />
                      </svg>
                    </>
                  ) : (
                    <>
                      <span className={`${cardTextClass}`}>최근 거래내역</span>
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        fill="none"
                        viewBox="0 0 24 24"
                        strokeWidth={1.5}
                        stroke="currentColor"
                        className="w-4 h-4"
                        style={{ stroke: isMainAccount ? "#FFFFFF" : "#6B7280" }}
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          d="M6 9l6 6 6-6"
                        />
                      </svg>
                    </>
                  )}
                </button>
              </div>
              {expandedAccountId === account.accountId && (
                <div className="mt-3 p-3 bg-white rounded-lg">
                  {Object.keys(groupedTransactions).map((date) => (
                    <div key={date} className="">
                      <p className="text-md text-gray-800">{date}</p>
                      {groupedTransactions[date].map((transaction, index) => {
                        const { time } = formatDate(transaction.tranDateTime);
                        return (
                          <div key={index} className="">
                            <div className="flex justify-between items-center">
                              <div className="flex justify-between items-center space-x-2">
                                <p className="text-xs text-gray-500 w-[50px]">
                                  {time}
                                </p>
                                <strong className="flex-1 text-md font-semibold text-gray-800 truncate">
                                  {transaction.printContent.includes("입금")
                                    ? "입금"
                                    : transaction.printContent.includes("출금")
                                    ? "출금"
                                    : transaction.printContent || "내용 없음"}
                                </strong>
                              </div>
                              <p
                                className={`font-semibold ${
                                  transaction.inoutType.includes("입금")
                                    ? "text-blue-500"
                                    : "text-red-500"
                                }`}
                              >
                                {Number(transaction.tranAmt).toLocaleString(
                                  "ko-KR"
                                )}{" "}
                                원
                              </p>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  ))}
                  {Object.keys(groupedTransactions).length === 0 && (
                    <p className="text-gray-500">거래 내역이 없습니다.</p>
                  )}
                </div>
              )}
            </div>
          );
        })}

        {!showAllAccounts && homeData.accounts.length > maxVisibleAccounts && (
          <button
            className="w-full py-2 text-lg font-medium bg-[#F5F5F5] rounded-lg mb-4"
            onClick={() => setShowAllAccounts(true)}
          >
            더보기
          </button>
        )}
        {showAllAccounts && (
          <button
            className="w-full py-2 text-lg font-medium bg-[#F5F5F5] rounded-lg mb-4"
            onClick={() => {
              setShowAllAccounts(false);
              handleScrollToTop();
            }}
          >
            접기
          </button>
        )}
      </>
    );
  };
  return (
    <div className="relative min-h-screen p-6" id="app-container">
      <header className="flex items-center justify-between">
        <h1 className="text-4xl text-[#212121] font-extrabold">ㅌㅋㅂㅋ</h1>
        <button
          onClick={() => navigate("/notice")}
          className="relative text-2xl"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width={30}
            height={30}
            viewBox="0 0 24 24"
          >
            <path
              fill="none"
              stroke="black"
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={1.5}
              d="M14.857 17.082a24 24 0 0 0 5.454-1.31A8.97 8.97 0 0 1 18 9.75V9A6 6 0 0 0 6 9v.75a8.97 8.97 0 0 1-2.312 6.022c1.733.64 3.56 1.085 5.455 1.31m5.714 0a24.3 24.3 0 0 1-5.714 0m5.714 0a3 3 0 1 1-5.714 0"
            ></path>
          </svg>
          {unreadNotificationCount > 0 && (
            <span className="absolute top-[-5px] right-[-5px] bg-red-500 text-white text-xs font-bold rounded-full w-5 h-5 flex items-center justify-center">
              {unreadNotificationCount}
            </span>
          )}
        </button>
      </header>
      {/* 새로고침 버튼 */}
      <button
        onClick={() => {
          setIsDataFetched(false); // 중복 방지 플래그 초기화
          fetchData()
        }}
        className="text-lg flex items-center ml-auto py-2"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width={20}
          height={20}
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          strokeWidth={2}
          className={"mr-1 w-4 h-4"}
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            d="M16.023 9.348h4.992v-.001M2.985 19.644v-4.992m0 0h4.992m-4.993 0 3.181 3.183a8.25 8.25 0 0 0 13.803-3.7M4.031 9.865a8.25 8.25 0 0 1 13.803-3.7l3.181 3.182m0-4.991v4.99"
          />
        </svg>
      </button>
      <main className="flex-1 flex flex-col overflow-y-auto justify-between pb-20">
        {authData?.mydataLinked === "Y" && isLoading ? (
          // 로딩 중 로고 애니메이션 표시
          <div className="h-40">
            <LogoLoader />
          </div>
        ) : authData?.mydataLinked === "Y" ? (
          // 로딩 완료 후 계좌 리스트 렌더링
          renderAccounts()
        ) : (
          // 계좌가 없을 때 계좌 연동 버튼 렌더링
          <button
            className="w-full py-3 mt-3 text-lg text-white font-medium bg-[#536DFE] rounded-lg"
            onClick={handleAccountLinkClick}
          >
            계좌 연동
          </button>
        )}
        <div className="flex justify-around">
          <div
            className="flex items-center justify-center w-[110px] h-[120px] bg-[#757575] text-[#F5F5F5] rounded-lg"
            onClick={() => {
              if (authData?.mydataLinked === "Y") {
                navigate("/settlement/memberselect");
              } else {
                alert(
                  "더치페이를 이용하려면 계좌 연동이 필요합니다. 계좌를 연동해주세요."
                );
              }
            }}
          >
            더치페이
          </div>
          <div
            className="flex items-center justify-center w-[110px] h-[120px] bg-[#757575] text-[#F5F5F5] rounded-lg"
            onClick={() => navigate("/addfriend")}
          >
            친구 추가
          </div>
          <div
            className="flex items-center justify-center w-[110px] h-[120px] bg-[#757575] text-[#F5F5F5] rounded-lg"
            onClick={() => navigate("/create-group")}
          >
            그룹 만들기
          </div>
        </div>
      </main>
      <footer>
        <BottomNav />
      </footer>
      <SMSVerificationModal
        isOpen={isSmsModalOpen}
        onClose={() => setIsSmsModalOpen(false)}
        onVerifySuccess={handleSmsVerifySuccess}
        purpose="pin"
        verificationId={null}
        tel={""}
        resendCooldown={0}
      />
      <PinCreationModal
        isOpen={isPinModalOpen}
        onClose={handlePinClose}
        onPinCreated={handlePinCreated}
      />
    </div>
  );
};
export default HomePage;
