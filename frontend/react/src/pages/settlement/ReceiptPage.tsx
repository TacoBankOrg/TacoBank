import React, { useEffect, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { uploadReceipt } from "../../api/ocrApi";
import { Item, ReceiptInfo, SelectedMemberInfo } from "../../types/ocrTypes";

const Receipt: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();

  // 로딩 상태 관리
  const [isLoading, setIsLoading] = useState(false);

  const groupId = location.state?.groupId || null;

  // 로컬 스토리지를 통해 데이터 관리
  const [selectedAccount] = useState(() => {
    const savedData = sessionStorage.getItem("selectedAccount");
    return savedData ? JSON.parse(savedData) : null;
  });

  const [receiptContent, setReceiptContent] = useState<Item[]>([]);
  const [totalAmount, setTotalAmount] = useState<number | null>(null);
  const [initialReceiptContent, setInitialReceiptContent] = useState<Item[]>([]);

  useEffect(() => {
    const updatedTotalAmount = receiptContent.reduce(
      (sum, item) => sum + (!item.inactive ? item.totalPrice : 0),
      0
    );

    // 상태가 변경될 때만 업데이트
    if (updatedTotalAmount !== totalAmount) {
      setTotalAmount(updatedTotalAmount);
    }
  }, [receiptContent, totalAmount]);

  // 상태 초기화
  const [receiptId, setReceiptId] = useState<number | null>(() => {
    const savedReceiptId = sessionStorage.getItem("receiptId");
    return savedReceiptId ? Number(savedReceiptId) : null;
  });

  const [groups] = useState<SelectedMemberInfo[]>(() => {
    const savedGroups = sessionStorage.getItem("groups");
    if (savedGroups) return JSON.parse(savedGroups);

    const selectedGroups = location.state?.selectedGroups || [];
    const selectedFriends = location.state?.selectedFriends || [];

    const groupMembers: SelectedMemberInfo[] =
      selectedGroups.length > 0
        ? selectedGroups.map(
            (groupMember: {
              memberId: number;
              memberName: string;
            }): SelectedMemberInfo => ({
              memberId: groupMember.memberId, // 숫자로 유지
              name: groupMember.memberName,
              amount: 0,
            })
          )
        : selectedFriends.map(
            (friend: { id: number; name: string }): SelectedMemberInfo => ({
              memberId: friend.id,
              name: friend.name,
              amount: 0,
            })
          );

    return groupMembers;
  });

  // 저장된 영수증 정보 로드 및 상태 업데이트
  useEffect(() => {
    const savedUpdatedItems = sessionStorage.getItem("updatedReceiptItems");
    const savedReceiptItems = sessionStorage.getItem("receiptItems");

    if (savedUpdatedItems) {
      // 수정된 데이터가 있으면 이를 로드
      const updatedItems = JSON.parse(savedUpdatedItems) as Item[];
      setReceiptContent(updatedItems);
    } else if (savedReceiptItems) {
      // 수정된 데이터가 없으면 초기 데이터를 로드
      const parsedItems = JSON.parse(savedReceiptItems) as Item[];
      const productWithGroups = parsedItems.map((item) => ({
        ...item,
        assignedTo: groups.map((group) => group.memberId), // 그룹 멤버 설정
        inactiveMembers: [], // 초기 비활성화 멤버
      }));
      setReceiptContent(productWithGroups);
      setInitialReceiptContent(productWithGroups);
    }
  }, [groups]);

  // receiptContent 변경 시 수정된 데이터를 저장 (초기 로드와 충돌 방지)
  useEffect(() => {
    // 초기 상태가 로드된 후에만 수정된 데이터를 저장
    if (receiptContent.length > 0 && receiptContent !== initialReceiptContent) {
      sessionStorage.setItem(
        "updatedReceiptItems",
        JSON.stringify(receiptContent)
      );
    }
  }, [receiptContent, initialReceiptContent]);

  useEffect(() => {
    // 로컬 스토리지에서 receiptId 가져오기
    const savedReceiptId = sessionStorage.getItem("receiptId");
    if (savedReceiptId) {
      setReceiptId(Number(savedReceiptId));
    }

    // receiptId 상태가 변경되면 로컬 스토리지에 저장
    if (receiptId !== null) {
      sessionStorage.setItem("receiptId", receiptId.toString());
    }
  }, [receiptId]);

  // 데이터 변경 시 로컬 스토리지 업데이트
  useEffect(() => {
    sessionStorage.setItem("selectedAccount", JSON.stringify(selectedAccount));
    sessionStorage.setItem("totalAmount", JSON.stringify(totalAmount));
    sessionStorage.setItem("receiptId", JSON.stringify(receiptId));
    sessionStorage.setItem("groups", JSON.stringify(groups));
  }, [selectedAccount, receiptContent, totalAmount, receiptId, groups]);

  // 품목별 금액을 그룹 멤버별로 나누고, 총액 계산
  useEffect(() => {
    calculateMemberTotals();
  }, [receiptContent]);

  const calculateMemberTotals = (): SelectedMemberInfo[] => {
    const totals: Record<number, number> = {};

    receiptContent.forEach((product) => {
      // 활성화된 멤버만 계산
      const activeMembers = product.assignedTo.filter(
        (memberId) => !product.inactiveMembers.includes(memberId)
      );
      const memberCount = activeMembers.length;

      if (memberCount === 0) return; // 활성화된 멤버가 없으면 건너뜀

      const baseAmount = Math.floor(product.totalPrice / memberCount);
      const remainder = product.totalPrice % memberCount;
      const randomIndex = Math.floor(Math.random() * memberCount);

      activeMembers.forEach((memberId, index) => {
        const amount = baseAmount + (index === randomIndex ? remainder : 0);
        totals[memberId] = (totals[memberId] || 0) + amount;
      });
    });

    return groups.map((group) => ({
      ...group,
      amount: totals[group.memberId] || 0, // 계산된 금액을 그룹별로 매핑
    }));
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      handleOCRRequest(file);
    }
  };

  const handleOCRRequest = async (file: File) => {
    setIsLoading(true); // 로딩 시작
    try {
      const response: ReceiptInfo = await uploadReceipt(file);
      console.log("OCR 인식 결과:", response);

      // 영수증 ID, 총 금액 저장
      setReceiptId(response.receiptId);
      setTotalAmount(response.totalAmount);

      const productWithGroups = response.items.map((item) => ({
        ...item,
        assignedTo: groups.map((group) => group.memberId), // 멤버 ID만 포함
        inactiveMembers: [], // 비활성화된 멤버 관리
      }));

      setReceiptContent(productWithGroups);

      // 영수증 정보 저장
      sessionStorage.setItem("receiptItems", JSON.stringify(response.items));
      sessionStorage.setItem("receiptId", JSON.stringify(response.receiptId));
    } catch (error) {
      console.error("OCR 요청 중 오류 발생:", error);
      alert("OCR 요청 중 오류가 발생했습니다. 다시 시도해주세요.");
    } finally {
      setIsLoading(false); // 로딩 종료
    }
  };

  useEffect(() => {
    const newReceiptContent = receiptContent.map((item) => {
      // 모든 멤버가 비활성화된 상태인지 확인
      const isInactive = item.assignedTo.every((memberId) =>
        item.inactiveMembers.includes(memberId)
      );

      // 상태가 동일하면 변경하지 않음
      if (item.inactive === isInactive) {
        return item;
      }

      return { ...item, inactive: isInactive };
    });

    // 상태가 변경된 경우에만 업데이트
    if (JSON.stringify(newReceiptContent) !== JSON.stringify(receiptContent)) {
      setReceiptContent(newReceiptContent);
    }
  }, [receiptContent]);

  // toggleMember 함수 수정
  const toggleMember = (productIndex: number, memberId: number) => {
    setReceiptContent((prevContent) =>
      prevContent.map((product, index) =>
        index === productIndex
          ? {
              ...product,
              inactiveMembers: product.inactiveMembers.includes(memberId)
                ? product.inactiveMembers.filter((id) => id !== memberId) // 활성화
                : [...product.inactiveMembers, memberId], // 비활성화
            }
          : product
      )
    );
  };

  const toggleItemInactive = (index: number) => {
    setReceiptContent((prev) =>
      prev.map((item, idx) =>
        idx === index
          ? {
              ...item,
              inactive: !item.inactive,
              // 모든 멤버를 비활성화 처리하여 UI 일관성 유지
              inactiveMembers: !item.inactive ? item.assignedTo : [],
            }
          : item
      )
    );

    // 총 금액 업데이트
    setTotalAmount((prevTotal) =>
      receiptContent.reduce(
        (sum, item, idx) =>
          idx === index
            ? sum + (item.inactive ? item.totalPrice : -item.totalPrice)
            : sum,
        prevTotal || 0
      )
    );
  };

  // 금액 초기화 핸들러
  const handleReset = () => {
    // 초기 영수증 상태로 복원
    setReceiptContent(initialReceiptContent); 
    // 총 금액 초기화
    const initialTotalAmount = initialReceiptContent.reduce(
      (sum, item) => sum + (!item.inactive ? item.totalPrice : 0),
      0
    );
    setTotalAmount(initialTotalAmount); // 총 금액을 초기 상태로 복원
    sessionStorage.removeItem("updatedReceiptItems"); // 수정된 항목 삭제
  };

  const handleNext = () => {
    // 멤버별 금액 계산
    const updatedGroups = calculateMemberTotals();

    const productMemberDetails = receiptContent
      .filter((item) => !item.inactive)
      .map((item) => ({
        productId: item.productId,
        productMembers: item.assignedTo.filter(
          (memberId) => !item.inactiveMembers.includes(memberId) // 비활성화된 멤버 제외
        ),
      }));

    navigate("/settlement/request", {
      state: {
        type: "receipt",
        selectedAccount,
        groups: updatedGroups, // 그룹 정보에 멤버별 금액이 포함됨
        totalAmount: totalAmount, // 최신 총액 반영
        groupId,
        receiptId: receiptId,
        productMemberDetails,
      },
    });
  };

  // 다시 불러오기 핸들러
  const handleReload = () => {
    const confirmReload = window.confirm(
      "현재 불러온 영수증 내역은 사라집니다. 영수증 다시 불러오기를 하시겠습니까?"
    );
    if (confirmReload) {
      // 로컬 스토리지 초기화
      sessionStorage.removeItem("receiptItems");
      sessionStorage.removeItem("updatedReceiptItems");
      sessionStorage.removeItem("totalAmount");
      sessionStorage.removeItem("receiptId");

      // 상태 초기화
      setReceiptContent([]);
      setIsLoading(false);
      setTotalAmount(null); // 총 금액 초기화
      setReceiptId(null); // receiptId 상태도 초기화
    } else {
      window.history.pushState(null, document.title, window.location.href);
    }
  };

  return (
    <div className="flex flex-col pt-6">
      {isLoading && (
        <div className="absolute inset-0 bg-gray-900 bg-opacity-50 flex items-center justify-center z-50">
          <div className="text-white text-lg">영수증 인식중..</div>
        </div>
      )}

      {/* 파일 업로드 */}
      {!receiptContent.length && !isLoading && (
        <div className="w-full max-w-md flex flex-col items-center">
          <label
            htmlFor="receipt-upload"
            className="w-full py-10 border-2 border-dashed border-gray-400 text-center text-gray-500 cursor-pointer rounded-lg"
          >
            <p className="text-lg font-medium">영수증 파일을 업로드해주세요</p>
            <p className="text-sm text-gray-400 mt-1">클릭하여 파일 선택</p>
          </label>
          <input
            type="file"
            id="receipt-upload"
            accept="image/*,application/pdf"
            onChange={handleFileChange}
            className="hidden"
          />
        </div>
      )}

      {/* OCR 결과 */}
      {receiptContent.length > 0 && (
        <div className="flex flex-col flex-grow">
          {/* 새로 불러오기 및 초기화 버튼 */}
          <div className="flex justify-between items-center mb-4">
            <button
              onClick={handleReset}
              className="bg-transparent text-gray-500 border border-gray-500 px-3 py-1 rounded-lg text-sm hover:bg-gray-100"
            >
              초기화
            </button>
            <button
              onClick={handleReload}
              className="bg-transparent text-[#536DFE] border border-[#536DFE] px-3 py-1 rounded-lg text-sm hover:bg-[#EAEDFF]"
            >
              영수증 새로 불러오기
            </button>
          </div>

          {/* 총 금액 수정 가능 */}
          <div className="flex justify-between items-center mb-4">
            <div className="flex items-center">
              <h1 className="text-xl font-bold text-gray-800 mr-2">총 금액</h1>
              <input
                type="text"
                value={totalAmount?.toLocaleString() || ""}
                onChange={(e) => {
                  const sanitizedValue = e.target.value.replace(/[^0-9]/g, ""); // 숫자 외 제거
                  const updatedTotal = sanitizedValue
                    ? parseInt(sanitizedValue, 10)
                    : 0;
                  setTotalAmount(updatedTotal); // 총 금액만 변경
                }}
                className="w-32 border border-gray-300 rounded-lg text-right px-2 py-1 focus:outline-none focus:border-[#536DFE] focus:ring-0"
              />
            </div>
          </div>

          {/* 품목별 가격 및 수정 */}
          {receiptContent.length > 0 && (
            <div className="flex flex-col flex-grow">
              {/* 품목별 정보 표시 */}
              {receiptContent.map((item, index) => (
                <div
                  key={`${item.productId}`}
                  className={`pb-4 border-b mb-4 ${
                    item.inactive ? "bg-gray-300 text-gray-500" : ""
                  }`}
                >
                  {/* 품목 이름과 삭제 버튼 */}
                  <div className="flex justify-between items-center mb-2">
                    <span
                      className={`font-bold ${
                        item.inactive ? "line-through" : ""
                      }`}
                    >
                      {item.name}
                    </span>
                    <button
                      onClick={() => toggleItemInactive(index)}
                      className="ml-4 px-2 py-1 rounded-lg border text-sm"
                    >
                      {item.inactive ? "✓" : "✕"}
                    </button>
                  </div>

                  {/* 품목 가격 */}
                  <div className="flex items-center mb-2">
                    <label
                      htmlFor={`price-${index}`}
                      className="text-gray-600 mr-2"
                    >
                      품목 가격
                    </label>
                    <input
                      id={`price-${index}`}
                      type="text"
                      value={item.totalPrice?.toString() || ""}
                      onChange={(e) => {
                        const sanitizedValue = e.target.value.replace(
                          /[^0-9]/g,
                          ""
                        );
                        const updatedPrice = sanitizedValue
                          ? parseInt(sanitizedValue, 10)
                          : 0;

                        setReceiptContent((prev) => {
                          const updatedContent = prev.map((product, idx) =>
                            idx === index
                              ? { ...product, totalPrice: updatedPrice }
                              : product
                          );
                          return updatedContent;
                        });
                      }}
                      className="w-32 border border-gray-300 rounded-lg text-right px-2 py-1 focus:outline-none focus:border-[#536DFE] focus:ring-0"
                      disabled={item.inactive}
                    />
                  </div>

                  {/* 품목별 멤버 */}
                  <div className="flex flex-wrap gap-2">
                    {item.assignedTo &&
                      Array.isArray(item.assignedTo) &&
                      item.assignedTo.map((memberId: number) => {
                        const group = groups.find(
                          (group) => group.memberId === memberId
                        );
                        const isInactive =
                          item.inactiveMembers?.includes(memberId);
                        return (
                          group && (
                            <div
                              key={`${item.productId}-${memberId}`}
                              onClick={() => toggleMember(index, memberId)}
                              className={`flex items-center gap-1 px-2 py-1 text-sm rounded-lg cursor-pointer ${
                                isInactive
                                  ? "bg-gray-300 text-gray-400"
                                  : "bg-[#EAEDFF] text-[#536DFE]"
                              }`}
                            >
                              <span
                                className={isInactive ? "line-through" : ""}
                              >
                                {group.name}
                              </span>
                              <span className="ml-2 text-gray-500 hover:text-gray-800">
                                {isInactive ? "+" : "✕"}
                              </span>
                            </div>
                          )
                        );
                      })}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* 다음 버튼 */}
      <div className="mt-auto">
        {/* 정산 총액 경고 메시지 */}
        {receiptContent.length > 0 &&
        (totalAmount === null || totalAmount === 0) ? (
          <p className="text-red-500 text-center mb-2">
            정산 총액은 0원 이상이어야 합니다.
          </p>
        ) : null}

        <button
          onClick={handleNext}
          disabled={
            !receiptContent.length ||
            !receiptId ||
            totalAmount === null ||
            totalAmount === 0
          }
          className={`w-full py-2 font-bold rounded-lg ${
            !receiptContent.length ||
            !receiptId ||
            totalAmount === null ||
            totalAmount === 0
              ? "bg-gray-300 text-gray-500 cursor-not-allowed"
              : "bg-[#536DFE] text-white hover:bg-[#485acf]"
          }`}
        >
          다음
        </button>
      </div>
      
    </div>
  );
};

export default Receipt;
