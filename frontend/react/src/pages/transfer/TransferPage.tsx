import React, { useEffect, useState } from "react";
import Select, { SingleValue } from "react-select";
import { useLocation, useNavigate } from "react-router-dom";
import backIcon from "../../assets/image/icon/backicon.png";
import { v4 as uuidv4 } from "uuid";
import { bankData } from "../../data/bankData";
import {
  favoriteAccountDelete,
  favoriteAccountSelect,
} from "../../api/accountApi";
import { checkReceiver, getTransferOption } from "../../api/transferApi";
import { Account, CheckReceiverReq } from "../../types/transferTypes";
import useAuth from "../../hooks/useAuth";
import StarIcon from "../../assets/image/icon/starIcon";
import { FavoriteAccountReq } from "../../types/accountTypes";
import { useAccountList } from "../../hooks/useAccountList";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import axios from "axios";
import { handleApiError } from "../../utils/apiHelpers";

const Transfer: React.FC = () => {
  const navigate = useNavigate();
  const { member } = useAuth();
  const queryClient = useQueryClient();
  const [selectedBankCode, setSelectedBankCode] = useState("");
  const [accountNumber, setAccountNumber] = useState("");
  const [menuIsOpen, setMenuIsOpen] = useState(false);
  const [activeTab, setActiveTab] = useState<"account" | "friends">("account");
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const location = useLocation();
  const [hasShownAlert, setHasShownAlert] = useState(false); // 메시지 표시 여부 관리
  const { accountId, accountName, banksCode } = location.state || {};
  const hiddenAccountId = location.state?.accountId;

  useEffect(() => {
    if (!location.state) {
      // 데이터가 없으면 이전 페이지로 리다이렉트
      navigate("/"); // -1은 브라우저 기록을 기반으로 이전 페이지로 이동
    }
  }, [location.state, navigate]);

  const memberId = member?.memberId

  // 데이터 로드
  // 계좌 리스트 가져오기
  const { data: myAccounts = [], isLoading: isAccountsLoading } =
    useAccountList(memberId as number);
  const { data: transferData, isLoading: transferOptionsLoading } = useQuery<{
    favoriteAccounts: Account[];
    recentAccounts: Account[];
    friendsAccounts: Account[];
  }>({
    queryKey: ["transferOptions", memberId],
    queryFn: async () => {
      const response = await getTransferOption(memberId as number);
      if (!response.data || response.status !== "SUCCESS") {
        throw new Error(response.message || "조회 실패");
      }
      // 응답 데이터를 명확히 구분해서 반환
      return {
        favoriteAccounts: response.data.favoriteAccounts || [],
        recentAccounts: response.data.recentAccounts || [],
        friendsAccounts: response.data.friendsAccounts || [],
      };
    },
  });
  const favoriteAccounts = transferData?.favoriteAccounts || [];
  const recentAccounts = transferData?.recentAccounts || [];
  const friends = transferData?.friendsAccounts || [];


  const filteredAccounts = myAccounts.filter(
    (account) => account.accountId !== hiddenAccountId
  );

  // 수취인 조회 함수
  const fetchReceiver = async (
    bankCode: string,
    accountNum: string,
    type: string
  ) => {
    setIsLoading(true);

    // 2초 후 요청 보내기
    setTimeout(async () => {
      const idempotencyKey = uuidv4();
      const dto: CheckReceiverReq = {
        idempotencyKey: idempotencyKey,
        withdrawalMemberId: memberId as number,
        withdrawalAccountId: accountId,
        receiverBankCode: bankCode,
        receiverAccountNum: accountNum,
      };

      console.log(dto);

      try {
        const response = await checkReceiver(dto);
        // 응답의 data 객체에서 필요한 값 추출
        console.log(response);
        if (response) {
          const accountId = response.withdrawalAccountId;
          const accountsNum = response.withdrawalAccountNum; // 나가는 계좌번호
          const balance = response.withdrawalBalance; // 계좌잔액
          const idempotencyKey = response.idempotencyKey; // 중복방지키
          const accountHolder = response.receiverAccountHolder; // 받는 사람
          navigate("/transfer/sendamount", {
            state: {
              type,
              banksCode, // 출금 은행 코드
              bankCode, // 입금 은행 코드
              accountNumber: accountNum, // 입금 계좌번호
              accountsNumber: accountsNum, //출금계좌번호
              accountsHolder: accountName, //출금 계좌주
              accountHolder, // 입금 계좌주
              idempotencyKey,
              balance, //출금 계좌 잔액
              accountId,
            },
          });
        } else {
          alert("수취인 정보를 확인할 수 없습니다. 다시 시도해주세요.");
        }
      } catch (error) {
        console.error("수취인 확인 중 오류:", error);
        alert("계좌번호 입력 오류입니다. 확인 후 다시 시도해주세요.");
      } finally {
        setIsLoading(false);
      }
    },2000); // 2초 지연
  };

  const isFavorite = (accountNum: string): boolean => {
    return favoriteAccounts.some(
      (account) => account.accountNum === accountNum
    );
  };

  // 최근 거래 계좌에서 즐겨찾기 계좌 제외
  const filteredRecentAccounts = recentAccounts.filter(
    (account) =>
      !favoriteAccounts.some(
        (favorite) => favorite.accountNum === account.accountNum
      )
  );

const toggleFavorite = async (accountNum: string) => {
    const isCurrentlyFavorite = isFavorite(accountNum);

  try {
    const dto: FavoriteAccountReq = {
      memberId: memberId as number,
      accountNum: accountNum,
    };

    if (isCurrentlyFavorite) {
      const response = await favoriteAccountDelete(dto);
      if (response.status === "SUCCESS") {
        // 즐겨찾기에서 제거
        queryClient.invalidateQueries({ queryKey: ["transferOptions", memberId] });
      } else {
        throw new Error("즐겨찾기 삭제에 실패했습니다.");
      }
    } else {
      const selectedAccount = [
        ...favoriteAccounts,
        ...myAccounts,
        ...recentAccounts,
      ].find((account) => account.accountNum === accountNum);

      if (!selectedAccount) {
        throw new Error("계좌 정보를 찾을 수 없습니다.");
      }

      const response = await favoriteAccountSelect(dto);
      if (response.status === "SUCCESS") {
        queryClient.invalidateQueries({ queryKey: ["transferOptions", memberId] });
      } else {
        throw new Error("즐겨찾기 추가에 실패했습니다.");
      }
    }
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const errorMessage = handleApiError(error); // 에러 메시지 처리
      alert(errorMessage);
    }
  }
};
  const handleNext = () => {
    if (!selectedBankCode) {
      alert("은행을 선택해주세요.");
      return;
    }

    const bank = bankData.find((b) => b.bankCode === selectedBankCode);
    if (bank && accountNumber.length !== bank.accountLength) {
      alert(`계좌번호는 ${bank.accountLength}자리여야 합니다.`);
      return;
    }

    if (selectedBankCode && accountNumber) {
      fetchReceiver(selectedBankCode, accountNumber, "manual");
    } else {
      alert("은행과 계좌번호를 입력해주세요.");
    }
  };

  const handleItemClick = (
    item: any,
    type: "account" | "friend" | "recent"
  ) => {
    const bankCode = item.bankCode || item.bankName;
    const accountNum = item.accountNum;
    fetchReceiver(bankCode, accountNum, type);
  };
  const handleBankChange = (
    selectedOption: SingleValue<{ value: string; label: string }>
  ) => {
    if (selectedOption) {
      setSelectedBankCode(selectedOption.value); // 은행 코드 설정
      setMenuIsOpen(false); // 토글 닫기
      setErrorMessage(null); // 에러 메시지 초기화
    }
  };

  const handleAccountNumberFocus = () => {
    if (!selectedBankCode) {
      if (!hasShownAlert) {
        alert("은행을 먼저 선택해주세요."); // alert 메시지 표시
        setHasShownAlert(true); // 메시지가 한 번 표시되었음을 기록
      }
      setMenuIsOpen(true); // 은행 선택 드롭다운 열기
    } else {
      setHasShownAlert(false); // 은행 선택된 경우 상태 초기화
    }
  };

  const handleAccountNumberPaste = (e: React.ClipboardEvent<HTMLInputElement>) => {
    e.preventDefault();
  
    const clipboardData = e.clipboardData.getData("text");
    console.log("Pasted data:", clipboardData);
  
    // 숫자만 추출
    const extractedAccountNumber = clipboardData.match(/\d+/g)?.join("") || "";
  
    const bank = bankData.find((b) => b.bankCode === selectedBankCode);
  
    if (bank && extractedAccountNumber.length > bank.accountLength) {
      setErrorMessage(
        `계좌번호는 ${bank.accountLength}자리까지만 입력 가능합니다.`
      );
    } else if (extractedAccountNumber) {
      setAccountNumber(extractedAccountNumber);
      setErrorMessage(null);
    } else {
      setErrorMessage("유효한 계좌번호를 붙여넣어 주세요.");
    }
  };

  const getBankName = (bankCode: string): string => {
    const bank = bankData.find((b) => b.bankCode === bankCode);
    return bank ? bank.bankName : "알 수 없는 은행";
  };
  const handleAccountNumberChange = (
    e: React.ChangeEvent<HTMLInputElement>
  ) => {
    const input = e.target.value.replace(/\D/g, ""); // 숫자만 입력 허용
    const bank = bankData.find((b) => b.bankCode === selectedBankCode);

    if (bank && input.length <= bank.accountLength) {
      setAccountNumber(input);
    }

    if (bank && input.length > bank.accountLength) {
      setErrorMessage(
        `계좌번호는 최대 ${bank.accountLength}자리까지만 입력 가능합니다.`
      );
    } else {
      setErrorMessage(null); // 에러 메시지 초기화
    }
  };

  const customStyles = {
    control: (base: any) => ({
      ...base,
      width: "100%",
      height: "42px",
      border: menuIsOpen ? "1px solid #536DFE" : "1px solid #ccc",
      boxShadow: "none",
      borderRadius: "6px",
      "&:hover": {
        borderColor: menuIsOpen ? "#536DFE" : "#ccc",
      },
    }),
    valueContainer: (base: any) => ({
      ...base,
      padding: "0 12px",
      height: "100%",
      display: "flex",
      alignItems: "center",
    }),
    menu: (base: any) => ({
      ...base,
      position: "absolute",
      marginTop: 0,
      boxShadow: "none",
      overflowY: "auto",
      backgroundColor: "white",
      zIndex: 999,
      border: "1px solid #ccc",
      borderRadius: "6px",
    }),
    option: (base: any) => ({
      ...base,
      backgroundColor: "white",
      color: "#262626",
      padding: "10px",
      "&:active": {
        backgroundColor: "white",
        color: "#262626",
      },
    }),
    placeholder: (base: any) => ({
      ...base,
      color: "#aaa",
      margin: "0",
      padding: "0",
    }),
    singleValue: (base: any) => ({
      ...base,
      color: "#262626",
      margin: "0",
      padding: "0",
    }),
    dropdownIndicator: (base: any, state: any) => ({
      ...base,
      transform: state.selectProps.menuIsOpen
        ? "rotate(180deg)"
        : "rotate(0deg)",
    }),
    indicatorSeparator: () => ({
      display: "none",
    }),
  };

  return (
    <div id="app-container" className="relative min-h-screen flex flex-col p-6">
      {/* 로딩 상태 */}
      {isLoading && (
        <div className="absolute inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="w-12 h-12 border-4 border-white border-t-transparent rounded-full animate-spin"></div>
        </div>
      )}

      {/* 메인 콘텐츠 */}
      <header className="relative flex items-center mb-6">
        <button className="absolute left-0 p-1" onClick={() => navigate("/")}>
          <img src={backIcon} alt="Back" className="w-8 h-8" />
        </button>
        <h1 className="text-3xl font-bold w-full text-center">
          송금하기
        </h1>
      </header>

      <main className="flex-1 flex flex-col overflow-y-auto">
        <div className="space-y-4">
          <Select
            options={bankData.map((bank) => ({
              value: bank.bankCode,
              label: bank.bankName,
            }))}
            placeholder="은행 선택"
            onChange={handleBankChange}
            isSearchable={false}
            styles={customStyles}
            blurInputOnSelect={true}
            menuIsOpen={menuIsOpen}
            onMenuOpen={() => setMenuIsOpen(true)}
            onMenuClose={() => setMenuIsOpen(false)}
          />
          <input
            type="text"
            placeholder="계좌번호를 입력하세요"
            value={accountNumber}
            onFocus={handleAccountNumberFocus}
            onChange={handleAccountNumberChange}
            onPaste={handleAccountNumberPaste}
            className="w-full px-3 py-2 border rounded-md border-gray-300 focus:outline-none focus:border-[#536DFE] focus:ring-0"
          />
          {errorMessage && (
            <p className="text-red-500 text-sm">{errorMessage}</p>
          )}
          <button
            onClick={handleNext}
            className={`w-full py-3 font-medium rounded-lg ${
              selectedBankCode && accountNumber
                ? "bg-[#536DFE] text-white hover:bg-[#485acf]" 
                : "bg-gray-300 text-gray-500"
            }`}
            disabled={!selectedBankCode || !accountNumber || isLoading}
          >
            다음
          </button>
        </div>

        <div className="mt-6">
          <div className="flex justify-around">
            <button
              className={`w-full py-2 ${
                activeTab === "account"
                  ? "text-[#536DFE] border-b-2 border-[#536DFE]"
                  : "text-gray-500"
              }`}
              onClick={() => setActiveTab("account")}
            >
              계좌번호
            </button>
            <button
              className={`w-full py-2 ${
                activeTab === "friends"
                  ? "text-[#536DFE] border-b-2 border-[#536DFE]"
                  : "text-gray-500"
              }`}
              onClick={() => setActiveTab("friends")}
            >
              친구
            </button>
          </div>

          <div className="mt-4">
            {isAccountsLoading || transferOptionsLoading ? (
              <p>데이터 로딩 중...</p>
            ) : activeTab === "account" ? (
              <>
                <h2 className="text-m font-medium mt-4 mb-2">내 계좌</h2>
                {filteredAccounts.map((account, index) => (
                  <div
                    key={index}
                    onClick={() => handleItemClick(account, "account")}
                    className="flex justify-between items-center p-4 cursor-pointer"
                  >
                    <div>
                      <p className="font-medium text-gray-800">
                        {account.accountHolder}
                      </p>
                      <p className="text-xs text-gray-600">
                        {account.bankName} {account.accountNum}
                      </p>
                    </div>
                  </div>
                ))}
                <h2 className="text-m font-medium mt-4 mb-2">즐겨찾기 계좌</h2>
                {favoriteAccounts.map((account, index) => (
                  <div
                    key={index}
                    onClick={() => handleItemClick(account, "account")}
                    className="flex justify-between items-center p-4 cursor-pointer"
                  >
                    <div>
                      <p className="font-medium text-gray-800">
                        {account.accountHolder}
                      </p>
                      <p className="text-xs text-gray-600">
                      {getBankName(account.bankCode)} {account.accountNum}
                      </p>
                    </div>
                    <button
                      onClick={(e) => {
                        e.stopPropagation(); // 클릭 이벤트 전파 중단
                        toggleFavorite(account.accountNum);
                      }}
                      className="ml-auto"
                    >
                      <StarIcon
                        filled={isFavorite(account.accountNum)}
                        size={24}
                      />
                    </button>
                  </div>
                ))}
                <h2 className="text-m font-medium mt-4 mb-2">최근 거래 계좌</h2>
                {filteredRecentAccounts.map((account, index) => (
                  <div
                    key={index}
                    onClick={() => handleItemClick(account, "recent")}
                    className="flex justify-between items-center p-4 cursor-pointer"
                  >
                    <div>
                      <p className="font-medium text-gray-800">
                        {account.accountHolder}
                      </p>
                      <p className="text-xs text-gray-600">
                      {getBankName(account.bankCode)} {account.accountNum}
                      </p>
                    </div>
                    <button
                      onClick={(e) => {
                        e.stopPropagation(); // 클릭 이벤트 전파 중단
                        toggleFavorite(account.accountNum);
                      }}
                      className="ml-auto"
                    >
                      <StarIcon
                        filled={isFavorite(account.accountNum)}
                        size={24}
                      />
                    </button>
                  </div>
                ))}
              </>
            ) : friends.length > 0 ? (
              friends.map((friend, index) => (
                <div
                  key={index}
                  onClick={() => handleItemClick(friend, "friend")}
                  className="p-4 flex justify-between items-center cursor-pointer"
                >
                  <p className="font-medium">{friend.accountHolder}</p>
                  <p className="text-xs text-gray-600">{getBankName(friend.bankCode)}{" "}{friend.accountNum}</p>
                </div>
              ))
            ) : (
              <button
              onClick={() => navigate("/addfriend")}
              className="px-3 py-2 w-full  bg-lime-500 text-white rounded-md"
            >
              친구 만들러가기
            </button>
            )}
          </div>
        </div>
      </main>
    </div>
  );
};

export default Transfer;
