import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import backIcon from "../../assets/image/icon/backicon.png";
import { useFriendList } from "../../hooks/useFriendList";
import { useLeaderGroupList } from "../../hooks/useLeaderGroupList.ts";
import { useAccountList } from "../../hooks/useAccountList";
import useAuth from "../../hooks/useAuth";
import { Friends } from "../../types/friendTypes";
import { Group } from "../../types/groupTypes";
import { Account } from "../../types/accountTypes";
import Select, { SingleValue } from "react-select";

const MemberSelect: React.FC = () => {
  const navigate = useNavigate();
  const { member } = useAuth(); // Member 정보
  const [friendSearchTerm, setFriendSearchTerm] = useState("");
  const [groupSearchTerm, setGroupSearchTerm] = useState("");
  const [selectedFriends, setSelectedFriends] = useState<number[]>([]);
  const [selectedGroups, setSelectedGroups] = useState<number[]>([]);
  const [selectedAccount, setSelectedAccount] = useState<Account | null>(null);
  const [menuIsOpen, setMenuIsOpen] = useState(false);
  const memberId = member?.memberId;
  // 친구 리스트 가져오기
  const { data: friends = [], isLoading: isFriendsLoading } = useFriendList(
    memberId as number
  );
  // 그룹 리스트 가져오기
  const { data: groups = [], isLoading: isGroupsLoading } = useLeaderGroupList(
    memberId as number
  );
  // 계좌 리스트 가져오기
  const { data: accounts = [], isLoading: isAccountsLoading } = useAccountList(
    memberId as number
  );

  useEffect(() => {
    if (!isAccountsLoading) {
      if (accounts.length > 0) {
        const mainAccount = member?.mainAccountId
          ? accounts.find(
              (account: Account) => account.accountId === member.mainAccountId
            )
          : accounts[0];
        if (mainAccount) {
          setSelectedAccount(mainAccount);
        }
      }
    }
  }, [accounts, member?.mainAccountId, isAccountsLoading]);

  const toggleFriendSelection = (friendId: number) => {
    setSelectedFriends((prevSelected) =>
      prevSelected.includes(friendId)
        ? prevSelected.filter((id) => id !== friendId)
        : [...prevSelected, friendId]
    );
    setSelectedGroups([]); // 친구 선택 시 그룹 초기화
  };

  // 그룹 선택을 위한 함수 (하나만 가능)
  const toggleGroupSelection = (groupId: number) => {
    setSelectedGroups((prevSelected) =>
      prevSelected.includes(groupId) ? [] : [groupId]
    );
    setSelectedFriends([]); // 그룹 선택 시 친구 초기화
  };

  const filteredFriends = Array.isArray(friends)
    ? friends.filter((friend: Friends) =>
        friend?.friendName
          ?.toLowerCase()
          .includes(friendSearchTerm.toLowerCase())
      )
    : [];

  const filteredGroups = Array.isArray(groups)
    ? groups.filter((group: Group) =>
        group?.groupName?.toLowerCase().includes(groupSearchTerm.toLowerCase())
      )
    : [];

  // selectedAccount 변경 시 sessionStorage 저장
  useEffect(() => {
    if (selectedAccount) {
      sessionStorage.setItem("selectedAccount", JSON.stringify(selectedAccount));
    }
  }, [selectedAccount]);

  const accountOptions = accounts.map((account) => ({
    value: account.accountId,
    label: `${account.bankName} ${account.accountNum} (${account.accountHolder})`,
  }));

  const handleAccountChange = (selectedOption: SingleValue<any>) => {
    const account = accounts.find(
      (acc) => acc.accountId === selectedOption?.value
    );
    setSelectedAccount(account || null);
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

  const handleBack = () => {
    sessionStorage.removeItem("selectedAccount");
    navigate("/");
  };

  // 브라우저 뒤로가기 감지
  useEffect(() => {
    const handlePopState = () => {
      handleBack(); // 뒤로가기 버튼 클릭 시 handleBack 실행
    };

    // popstate 이벤트 리스너 추가
    window.addEventListener("popstate", handlePopState);

    // 컴포넌트 언마운트 시 리스너 제거
    return () => {
      window.removeEventListener("popstate", handlePopState);
    };
  }, []);

  return (
    <div id="app-container" className="min-h-screen p-6 pb-20">
      <header className="relative flex items-center mb-6">
        <button className="absolute left-0 p-1" onClick={handleBack}>
          <img src={backIcon} alt="Back" className="w-8 h-8" />
        </button>
        <h1 className="text-3xl font-bold w-full text-center">
          정산멤버 선택
        </h1>
      </header>

      <Select
        options={accountOptions}
        value={
          selectedAccount
            ? {
                value: selectedAccount.accountId,
                label: `${selectedAccount.bankName} ${selectedAccount.accountNum}`,
              }
            : null
        }
        onChange={handleAccountChange}
        isSearchable={false}
        styles={customStyles}
        blurInputOnSelect={true}
        menuIsOpen={menuIsOpen}
        onMenuOpen={() => setMenuIsOpen(true)}
        onMenuClose={() => setMenuIsOpen(false)}
      />
      {/* 친구 & 그룹 리스트 */}
      <div className="flex flex-col mt-4 gap-4">
        {/* 친구 리스트 */}
        <div className="flex flex-col rounded-md overflow-y-auto">
          <div className="flex items-center mb-4">
            <p className="text-lg font-bold mr-4 flex-shrink-0">친구</p>
            <div className="flex items-center border-b border-gray-300 p-1 w-full">
              <input
                type="text"
                placeholder="친구명 검색"
                value={friendSearchTerm}
                onChange={(e) => setFriendSearchTerm(e.target.value)}
                className="ml-2 w-full bg-transparent focus:outline-none"
              />
            </div>
          </div>
          {isFriendsLoading ? (
            <p className="text-gray-500 text-sm mt-2">
              친구 목록을 불러오는 중...
            </p>
          ) : filteredFriends.length > 0 ? (
            filteredFriends.map((friend: Friends) => (
              <label
                key={friend.friendId}
                className="p-2 rounded-md flex items-center cursor-pointer"
                onClick={() => toggleFriendSelection(friend.friendId)}
              >
                <input
                  type="radio"
                  className="form-radio h-5 w-5 rounded-full cursor-pointer"
                  checked={selectedFriends.includes(friend.friendId)}
                  onChange={() => toggleFriendSelection(friend.friendId)}
                  onClick={(e) => e.stopPropagation()} // 클릭 이벤트 버블링 방지
                />
                <span className="ml-3 text-gray-800">{friend.friendName}</span>
              </label>
            ))
          ) : (
            // 친구가 없을 때 버튼 표시
            <button
              onClick={() => navigate("/addfriend")}
              className="mt-2 p-2 bg-[#536DFE] text-white rounded-md"
            >
              친구 만들러가기
            </button>
          )}
        </div>

        {/* 그룹 리스트 */}
        <div className="flex flex-col rounded-md overflow-y-auto mt-4">
          <div className="flex items-center mb-4">
            <p className="text-lg font-bold mr-4 flex-shrink-0">그룹</p>
            <div className="flex items-center border-b border-gray-300 p-1 w-full">
              <input
                type="text"
                placeholder="그룹명 검색"
                value={groupSearchTerm}
                onChange={(e) => setGroupSearchTerm(e.target.value)}
                className="ml-2 w-full bg-transparent focus:outline-none"
              />
            </div>
          </div>
          {isGroupsLoading ? (
            <p className="text-gray-500 text-sm mt-2">
              그룹 목록을 불러오는 중...
            </p>
          ) : filteredGroups.length > 0 ? (
            filteredGroups.map((group: Group) => (
              <label
                key={group.groupId}
                className={"p-2 rounded-md flex items-center cursor-pointer"}
                onClick={() => toggleGroupSelection(group.groupId)}
              >
                <input
                  type="radio"
                  className={"form-radio h-5 w-5 rounded-full "}
                  checked={selectedGroups.includes(group.groupId)}
                  onChange={() => toggleGroupSelection(group.groupId)}
                  onClick={(e) => e.stopPropagation()} // 클릭 이벤트 버블링 방지
                />
                <span className="ml-3 text-gray-800">{group.groupName}</span>
              </label>
            ))
          ) : (
            // 그룹이 없을 때 버튼 표시
            <button
              onClick={() => navigate("/create-group")}
              className="mt-2 p-2 bg-[#EAEDFF] text-white rounded-md"
            >
              그룹 생성하러가기
            </button>
          )}
        </div>
      </div>

      {/* 다음 버튼 */}
      <div className="mt-auto">
        <button
          className={`w-full py-2 ${
            selectedFriends.length > 0 || selectedGroups.length > 0
              ? "bg-[#536DFE] text-white"
              : "bg-gray-300 text-gray-500 cursor-not-allowed"
          } font-bold rounded-lg`}
          onClick={() => {
            if (selectedFriends.length > 0 || selectedGroups.length > 0) {
              const selectedGroupMembers = selectedGroups.length
                ? (groups as Group[])
                    .filter((group: Group) =>
                      selectedGroups.includes(group.groupId)
                    )
                    .flatMap((group: Group) => {
                      // 멤버 목록을 먼저 가져옴
                      const allMembers = group.members.map((member) => ({
                        memberId: member.memberId,
                        memberName: member.memberName,
                        amount: 0, // 기본값
                      }));

                      // 리더 정보를 멤버 목록에 추가 (중복 방지)
                      const isLeaderIncluded = group.members.some(
                        (member) => member.memberId === group.leaderId
                      );

                      if (!isLeaderIncluded) {
                        allMembers.push({
                          memberId: group.leaderId,
                          memberName: group.leaderName,
                          amount: 0,
                        });
                      }

                      return allMembers;
                    })
                : [];

              const selectedFriendsWithNames = selectedFriends.map(
                (friendId) => {
                  const friend = (friends as Friends[]).find(
                    (f) => f.friendId === friendId
                  );
                  return {
                    id: friendId,
                    name: friend ? friend.friendName : `친구 ${friendId}`,
                  };
                }
              );

              const groupId =
                selectedGroups.length > 0 ? selectedGroups[0] : null; // 첫 번째 그룹 ID

              navigate("/settlement/action", {
                state: {
                  selectedAccount,
                  selectedFriends: selectedFriendsWithNames, // 이름 포함
                  selectedGroups: selectedGroupMembers, // 그룹 멤버
                  groupId, // 단일 그룹 ID 전달
                },
              });
            }
          }}
          disabled={selectedFriends.length === 0 && selectedGroups.length === 0}
        >
          다음
        </button>
      </div>
    </div>
  );
};

export default MemberSelect;
