import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { getTransferList } from "../../api/accountApi";
import { TransactionReq } from "../../types/transactionTypes";
import backIcon from "../../assets/image/icon/backicon.png";
import LogoLoader from "../../components/LogoLoader";

interface Transaction {
  tranNum: string;
  type: string;
  inoutType: string;
  amount: number;
  afterBalanceAmount: number;
  tranDateTime: string;
  tranAmt: string;
  printContent: string;
}

interface SelectedAccount {
  accountId: number;
  accountName: string;
  accountNum: string;
  accountHolder: string;
  bankName: string;
  bankCode: string;
  balance: string;
  transactionList: Transaction[];
}

const AccountDetail: React.FC = () => {
  const navigate = useNavigate();
  const { accountId } = useParams<{ accountId: string }>();
  const parsedAccountId = Number(accountId);

  const [isMonthSelectVisible, setIsMonthSelectVisible] = useState(false);

  const [selectedAccount, setSelectedAccount] =
    useState<SelectedAccount | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isError, setIsError] = useState<boolean>(false);
  const [groupedTransactions, setGroupedTransactions] = useState<
    Record<string, Transaction[]>
  >({});
  const [showSlide, setShowSlide] = useState(false);
  const [isDirectSetting, setIsDirectSetting] = useState<boolean>(false);
  const [displayedType, setDisplayedType] = useState<string>("전체");
  const [displayedOrder, setDisplayedOrder] = useState<string>("최신순");
  const [selectedRange, setSelectedRange] = useState<
    "1주일" | "1개월" | "3개월" | "월별" | "직접설정"
  >("1개월");
  const [selectedType, setSelectedType] = useState<"전체" | "입금" | "출금">(
    "전체"
  );
  const [selectedOrder, setSelectedOrder] = useState<"최신순" | "과거순">(
    "최신순"
  );

  const today = new Date();
  const firstDayOfMonth = new Date(today.getFullYear(), today.getMonth(), 1);

  const getFormattedDate = (date: Date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}${month}${day}`; // "YYYYMMDD" 형식
  };

  const [displayedFromDate, setDisplayedFromDate] = useState<string>(
    getFormattedDate(firstDayOfMonth) // 이번 달 첫날
  );
  const [displayedToDate, setDisplayedToDate] = useState<string>(
    getFormattedDate(today) // 오늘 날짜
  );

  const [fromDate, setFromDate] = useState<Date>(firstDayOfMonth); // 이번 달 첫날
  const [toDate, setToDate] = useState<Date>(today); // 오늘 날짜
  const [displayedRange, setDisplayedRange] = useState<string>(
    `${today.getFullYear()}년 ${String(today.getMonth() + 1).padStart(
      2,
      "0"
    )}월` // 기본 표시값
  );

  const toggleSlide = () => {
    setShowSlide(!showSlide); // 슬라이드 상태 토글
  };

  const fetchAccountDetails = async (fromDate: string, toDate: string) => {
    setIsLoading(true); // 로딩 시작
    const minimumLoadingTime = new Promise((resolve) =>
      setTimeout(resolve, 1000)
    );

    const dto: TransactionReq = {
      accountId: parsedAccountId,
      fromDate,
      toDate,
    };

    try {
      const response = await getTransferList(dto); // 서버 요청
      setSelectedAccount(response); // 상태 업데이트
      await minimumLoadingTime;
      return response; // 결과 반환
    } catch (error) {
      console.error("데이터 요청 실패:", error);
      setIsError(true);
      return null; // 실패 시 null 반환
    } finally {
      setIsLoading(false); // 로딩 종료
    }
  };

  useEffect(() => {
    const fetchInitialData = async () => {
      const formattedFromDate = getFormattedDate(firstDayOfMonth);
      const formattedToDate = getFormattedDate(today);

      const accountDetails = await fetchAccountDetails(
        formattedFromDate,
        formattedToDate
      );

      if (accountDetails && accountDetails.transactionList) {
        const grouped = groupTransactionsByDate(accountDetails.transactionList);
        setGroupedTransactions(grouped); // 초기 데이터 설정
      }
    };

    fetchInitialData();
  }, [parsedAccountId]);

  const handleApplyFilters = async () => {
    if (!fromDate || !toDate) {
      alert("조회기간을 설정해주세요.");
      return;
    }

    setShowSlide(false); // 모달 닫기
    setIsLoading(true); // 로딩 상태 활성화

    const formattedFromDate = getFormattedDate(fromDate);
    const formattedToDate = getFormattedDate(toDate);

    try {
      // 서버에서 데이터 가져오기
      const accountDetails = await fetchAccountDetails(
        formattedFromDate,
        formattedToDate
      );

      if (accountDetails && accountDetails.transactionList) {
        // 필터 및 정렬 적용
        const filteredAndSortedTransactions = accountDetails.transactionList
          .filter((transaction: Transaction) => {
            if (selectedType === "전체") return true;
            if (selectedType === "입금")
              return transaction.inoutType?.includes("입금");
            return !transaction.inoutType?.includes("입금");
          })
          .sort((a: Transaction, b: Transaction) => {
            // tranDateTime에서 공백 제거 후 Date 객체로 변환
            const dateA = new Date(
              `${a.tranDateTime.slice(0, 4)}-${a.tranDateTime.slice(
                4,
                6
              )}-${a.tranDateTime.slice(6, 8)}T${a.tranDateTime.slice(
                9,
                11
              )}:${a.tranDateTime.slice(11, 13)}:${a.tranDateTime.slice(13)}`
            ).getTime();

            const dateB = new Date(
              `${b.tranDateTime.slice(0, 4)}-${b.tranDateTime.slice(
                4,
                6
              )}-${b.tranDateTime.slice(6, 8)}T${b.tranDateTime.slice(
                9,
                11
              )}:${b.tranDateTime.slice(11, 13)}:${b.tranDateTime.slice(13)}`
            ).getTime();

            return selectedOrder === "최신순" ? dateB - dateA : dateA - dateB;
          });

        // 거래 내역을 날짜별로 그룹화
        const grouped = groupTransactionsByDate(filteredAndSortedTransactions);
        setGroupedTransactions(grouped); // 상태 업데이트
      }

      if (selectedRange === "월별") {
        const currentYear = fromDate.getFullYear();
        const currentMonth = fromDate.getMonth();
        setDisplayedRange(
          `${currentYear}년 ${String(currentMonth + 1).padStart(2, "0")}월`
        );
      }

      // 상태 업데이트
      setDisplayedRange(selectedRange);
      setDisplayedType(selectedType);
      setDisplayedOrder(selectedOrder);
      setIsError(false);
    } catch (error) {
      console.error("필터 처리 중 오류:", error);
      setIsError(true);
    } finally {
      setIsLoading(false); // 로딩 종료
    }
  };

  useEffect(() => {
    // useEffect 삭제됨
    // 데이터를 자동으로 요청하지 않음
  }, [parsedAccountId]); // parsedAccountId 또는 날짜가 변경될 때마다 호출

  const handleRangeSelect = (
    range: "1주일" | "1개월" | "3개월" | "월별" | "직접설정"
  ) => {
    const today = new Date();

    if (range === "월별") {
      setIsMonthSelectVisible((prev) => !prev);
      if (!isMonthSelectVisible) {
        const currentYear = fromDate.getFullYear();
        const currentMonth = fromDate.getMonth();

        const startDate = new Date(currentYear, currentMonth, 1);
        const endDate = new Date(currentYear, currentMonth + 1, 0);

        setFromDate(startDate);
        setToDate(endDate);
        setDisplayedRange(
          `${startDate.getFullYear()}년 ${String(
            startDate.getMonth() + 1
          ).padStart(2, "0")}월`
        );
      }
      setIsDirectSetting(false);
    } else if (range === "직접설정") {
      setIsDirectSetting((prev) => !prev);
      if (!isDirectSetting) {
        setFromDate(today);
        setToDate(today);
      }
    } else {
      let calculatedFromDate: Date | null = null;

      setIsDirectSetting(false);
      switch (range) {
        case "1주일":
          calculatedFromDate = new Date(today);
          calculatedFromDate.setDate(today.getDate() - 7);
          break;
        case "1개월":
          calculatedFromDate = new Date(today);
          calculatedFromDate.setMonth(today.getMonth() - 1);
          break;
        case "3개월":
          calculatedFromDate = new Date(today);
          calculatedFromDate.setMonth(today.getMonth() - 3);
          break;
        default:
          calculatedFromDate = new Date(today);
      }

      setFromDate(calculatedFromDate);
      setToDate(today);
      setDisplayedFromDate(getFormattedDate(calculatedFromDate));
      setDisplayedToDate(getFormattedDate(today));
      setSelectedRange(range);
      // 기존 범위 표시값 설정
      setDisplayedRange(range);
    }

    setSelectedRange(range);
  };

  useEffect(() => {
    if (selectedRange === "월별") {
      const currentYear = fromDate.getFullYear();
      const currentMonth = fromDate.getMonth();

      setDisplayedRange(
        `${currentYear}년 ${String(currentMonth + 1).padStart(2, "0")}월`
      );
    }
  }, [fromDate, selectedRange]);

  const toggleModal = () => setShowSlide((prev) => !prev);

  if (!parsedAccountId) return <p>로딩 중...</p>;

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

  const groupTransactionsByDate = (transactions: Transaction[]) => {
    if (!transactions || transactions.length === 0) return {};

    return transactions.reduce(
      (groups: Record<string, Transaction[]>, transaction) => {
        const { date } = formatDate(transaction.tranDateTime);
        if (!groups[date]) {
          groups[date] = [];
        }
        groups[date].push(transaction);
        return groups;
      },
      {}
    );
  };

  // 선택된 계좌가 없는 경우 처리
  if (!parsedAccountId) {
    return (
      <div className="flex justify-center items-center h-screen">
        <p>선택된 계좌를 찾을 수 없습니다. 계좌 정보를 확인해주세요.</p>
      </div>
    );
  }

  return (
    <div id="app-container" className="min-h-screen flex flex-col p-6 relative">
      {isLoading && !selectedAccount ? (
        <div className="flex justify-center items-center h-screen">
          <LogoLoader />
        </div>
      ) : isError ? (
        /* 에러 발생 시 */
        <div className="flex justify-center items-center h-screen">
          <p className="text-red-500">
            오류가 발생했습니다. 데이터를 불러올 수 없습니다.
          </p>
        </div>
      ) : !selectedAccount ? (
        /* 계좌가 선택되지 않은 경우 (예외적으로 추가 처리) */
        <div className="flex justify-center items-center h-screen">
          <p className="text-gray-500">
            선택된 계좌 정보가 없습니다. 다시 시도해주세요.
          </p>
        </div>
      ) : (
        <>
          {/* 상단 바 */}
          <header className="relative flex items-center mb-6">
            <button
              className="absolute left-0 p-1"
              onClick={() => navigate(-1)}
            >
              <img src={backIcon} alt="Back" className="w-8 h-8" />
            </button>
            <h1 className="text-3xl font-bold w-full text-center">
              {selectedAccount.accountName}
            </h1>
          </header>
          <div className="flex flex-col items-center justify-center space-y-2">
            <p
              className="text-sm text-gray-600 cursor-pointer underline"
              onClick={(e) => {
                e.stopPropagation();
                const textToCopy = `${selectedAccount.bankName} ${selectedAccount.accountNum}`;
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
              {selectedAccount.bankName} {selectedAccount.accountNum}
            </p>
            <span className="text-2xl font-bold">
              {selectedAccount.balance}원
            </span>
          </div>

          <div className="flex justify-between items-center bg-gray-50 p-2 rounded-md">
            <p className="text-gray-600 text-xs">
              {formatDate(displayedFromDate).date} ~{" "}
              {formatDate(displayedToDate).date}
            </p>
            <div className="flex justify-between items-center space-x-1">
              <span className="text-gray-800 text-xs">
                {displayedRange} ∙ {displayedType} ∙ {displayedOrder}
              </span>
              <button onClick={toggleModal}>
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                  strokeWidth={2.0}
                  stroke="currentColor"
                  className="w-4 h-4"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    d="m19.5 8.25-7.5 7.5-7.5-7.5"
                  />
                </svg>
              </button>
            </div>
          </div>
          {/* 거래 내역 카드 리스트 */}
          <section
            className={`flex-1 bg-white p-4 space-y-4 ${
              showSlide ? "overflow-hidden" : "overflow-y-auto"
            }`}
          >
            {isLoading ? (
              <div className="flex justify-center items-center h-64">
                <LogoLoader />
              </div>
            ) : Object.keys(groupedTransactions).length === 0 ? (
              <div className="flex justify-center text-center items-center">
                <p className="text-gray-500">거래 내역이 없습니다.</p>
              </div>
            ) : (
              Object.entries(groupedTransactions).map(
                ([date, transactions]) => (
                  <div key={date}>
                    <h2 className="text-xs font-semibold text-gray-800 mb-2">
                      {date}
                    </h2>
                    {transactions.map((transaction) => (
                      <div
                        key={transaction.tranNum}
                        className="p-4 border-b border-gray-300"
                      >
                        <div className="flex justify-between items-center">
                          <p className="text-sm text-gray-600">
                            {formatDate(transaction.tranDateTime).time}
                          </p>
                          <p
                            className={`text-sm font-normal ${
                              transaction.inoutType &&
                              transaction.inoutType.includes("입금")
                                ? "text-blue-500"
                                : "text-red-500"
                            }`}
                          >
                            {transaction.inoutType &&
                            transaction.inoutType.includes("입금")
                              ? "입금"
                              : "출금"}
                          </p>
                        </div>
                        <div className="flex justify-between items-center">
                          <h3 className="text-md font-bold text-gray-800">
                            {transaction.printContent || "내용 없음"}
                          </h3>
                          <p className="font-semibold text-gray-800">
                            {Number(transaction.tranAmt).toLocaleString()} 원
                          </p>
                        </div>

                        <div className="flex justify-end items-center">
                          <p className="text-sm text-gray-500">
                            잔액{" "}
                            {Number(
                              transaction.afterBalanceAmount
                            ).toLocaleString()}{" "}
                            원
                          </p>
                        </div>
                      </div>
                    ))}
                  </div>
                )
              )
            )}
          </section>
          {showSlide && (
            <div
              className="absolute bottom-0 left-0 w-full bg-white border-x border-t border-gray-300 pb-4 px-4 z-50 max-h-[100vh] pb-[env(safe-area-inset-bottom,20px)]  min-h-[45vh] rounded-t-3xl flex flex-col"
            >
              <div className="mt-4">
                <div className="flex justify-between items-center mb-4">
                  <h2 className="text-lg font-semibold">조회 조건 설정</h2>
                  <button onClick={toggleSlide}>
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      fill="none"
                      viewBox="0 0 24 24"
                      strokeWidth={1.5}
                      stroke="currentColor"
                      className="size-6"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        d="M6 18 18 6M6 6l12 12"
                      />
                    </svg>
                  </button>
                </div>
                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium mb-1">
                      조회기간
                    </label>
                    <div className="flex space-x-2">
                      <button
                        onClick={() => handleRangeSelect("1주일")}
                        className={`border rounded-md py-2 px-4 flex-1 text-sm hover:bg-gray-100 ${
                          selectedRange === "1주일" ? "bg-gray-200" : ""
                        }`}
                      >
                        1주일
                      </button>
                      <button
                        onClick={() => handleRangeSelect("1개월")}
                        className={`border rounded-md py-2 px-4 flex-1 text-sm hover:bg-gray-100 ${
                          selectedRange === "1개월" ? "bg-gray-200" : ""
                        }`}
                      >
                        1개월
                      </button>
                      <button
                        onClick={() => handleRangeSelect("3개월")}
                        className={`border rounded-md py-2 px-4 flex-1 text-sm hover:bg-gray-100 ${
                          selectedRange === "3개월" ? "bg-gray-200" : ""
                        }`}
                      >
                        3개월
                      </button>
                    </div>
                    <div className="flex mt-2 space-x-2">
                      <button
                        onClick={() => handleRangeSelect("월별")}
                        className={`border rounded-md py-2 px-4 flex-1 text-sm hover:bg-gray-100 ${
                          selectedRange === "월별" ? "bg-gray-200" : ""
                        }`}
                      >
                        월별
                      </button>
                      <button
                        onClick={() => handleRangeSelect("직접설정")}
                        className={`border rounded-md py-2 px-4 flex-1 text-sm hover:bg-gray-100 ${
                          selectedRange === "직접설정" ? "bg-gray-200" : ""
                        }`}
                      >
                        직접설정
                      </button>
                    </div>
                  </div>
                  {selectedRange === "월별" && isMonthSelectVisible && (
                    <div className="flex space-x-2 items-center mt-2">
                      <input
                        type="month"
                        className="border rounded-md py-2 px-4 flex-1 text-sm"
                        value={
                          fromDate
                            ? `${fromDate.getFullYear()}-${String(
                                fromDate.getMonth() + 1
                              ).padStart(2, "0")}`
                            : `${new Date().getFullYear()}-${String(
                                new Date().getMonth() + 1
                              ).padStart(2, "0")}`
                        }
                        onChange={(e) => {
                          const [year, month] = e.target.value.split("-");
                          const startDate = new Date(
                            parseInt(year),
                            parseInt(month) - 1,
                            1
                          );
                          const endDate = new Date(
                            parseInt(year),
                            parseInt(month),
                            0
                          ); // 마지막 날짜 계산
                          setFromDate(startDate);
                          setToDate(endDate);
                        }}
                      />
                    </div>
                  )}
                  {isDirectSetting && selectedRange && (
                    <div className="flex space-x-2 items-center">
                      <input
                        type="date"
                        className="border rounded-md py-2 px-4 flex-1 text-sm"
                        value={
                          fromDate ? fromDate.toISOString().split("T")[0] : ""
                        }
                        onChange={(e) => setFromDate(new Date(e.target.value))}
                      />
                      <span>~</span>
                      <input
                        type="date"
                        className="border rounded-md py-2 px-4 flex-1 text-sm"
                        value={toDate ? toDate.toISOString().split("T")[0] : ""}
                        onChange={(e) => setToDate(new Date(e.target.value))}
                      />
                    </div>
                  )}
                  <div>
                    <label className="block text-sm font-medium mb-1">
                      유형
                    </label>
                    <div className="flex space-x-2">
                      <button
                        onClick={() => setSelectedType("전체")}
                        className={`border rounded-md py-2 px-4 flex-1 text-sm hover:bg-gray-100 ${
                          selectedType === "전체" ? "bg-gray-200" : ""
                        }`}
                      >
                        전체
                      </button>
                      <button
                        onClick={() => setSelectedType("입금")}
                        className={`border rounded-md py-2 px-4 flex-1 text-sm hover:bg-gray-100 ${
                          selectedType === "입금" ? "bg-gray-200" : ""
                        }`}
                      >
                        입금
                      </button>
                      <button
                        onClick={() => setSelectedType("출금")}
                        className={`border rounded-md py-2 px-4 flex-1 text-sm hover:bg-gray-100 ${
                          selectedType === "출금" ? "bg-gray-200" : ""
                        }`}
                      >
                        출금
                      </button>
                    </div>
                  </div>
                  <div>
                    <label className="block text-sm font-medium mb-1">
                      정렬
                    </label>
                    <div className="flex space-x-2">
                      <button
                        onClick={() => setSelectedOrder("최신순")}
                        className={`border rounded-md py-2 px-4 flex-1 text-sm hover:bg-gray-100 ${
                          selectedOrder === "최신순" ? "bg-gray-200" : ""
                        }`}
                      >
                        최신순
                      </button>
                      <button
                        onClick={() => setSelectedOrder("과거순")}
                        className={`border rounded-md py-2 px-4 flex-1 text-sm hover:bg-gray-100 ${
                          selectedOrder === "과거순" ? "bg-gray-200" : ""
                        }`}
                      >
                        과거순
                      </button>
                    </div>
                  </div>
                </div>
                <button
                  onClick={handleApplyFilters}
                  className="mt-6 w-full bg-blue-500 text-white py-2 rounded-md hover:bg-blue-600"
                >
                  조회
                </button>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default AccountDetail;
