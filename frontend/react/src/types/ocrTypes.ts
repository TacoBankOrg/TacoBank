export interface ReceiptUploadResponse {
  status: string;
  message: string;
  data: ReceiptInfo;
}

export interface ReceiptInfo {
  receiptId: number;
  totalAmount: number;
  items: Item[];
}

export interface Item {
  productId: number;
  number: number;
  name: string;
  totalPrice: number;
  assignedTo: number[];
  inactiveMembers: number[];
  inactive: boolean;
}

export interface SelectedMemberInfo {
  memberId: number;
  name: string;
  amount: number;
}