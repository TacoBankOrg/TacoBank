import { Routes, Route, Navigate } from "react-router-dom";
import "./app.css";
import Splash from "./pages/SplashPage";
import Start from "./pages/StartPage";
import Login from "./pages/auth/LoginPage";
import Join from "./pages/auth/JoinPage";
import FindEmail from "./pages/auth/FindEmailPage";
import FindPassword from "./pages/auth/FindPasswordPage";
import Home from "./pages/HomePage";
import FriendList from "./pages/friends/FriendListPage";
import GroupList from "./pages/groups/GroupListPage";
import AddFriend from "./pages/friends/AddFriendPage";
import ResetPassword from "./pages/auth/ResetPasswordPage";
import BlockedList from "./pages/friends/BlockedListPage";
import CreateGroup from "./pages/groups/CreateGroupPage";
import GroupDetail from "./pages/groups/GroupDetailPage";
import MemberSelect from "./pages/settlement/MemberSelectPage";
import Split from "./pages/settlement/SplitPage";
import Receipt from "./pages/settlement/ReceiptPage";
import SettlementReq from "./pages/settlement/SettlementReqPage";
import SettlementComplete from "./pages/settlement/SettlementCompletePage";
import ReceiveMoney from "./pages/settlement/status/ReceiveMoneyPage";
import SendMoney from "./pages/settlement/status/SendMoneyPage";
import ReceiveDetail from "./pages/settlement/status/ReceiveDetailPage";
import MyInfo from "./pages/mypage/MyInfoPage.tsx";
import ChangePassword from "./pages/mypage/ChangePassword.tsx";
import Settlement from "./pages/settlement/status/SettlementPage.tsx";
import ChangePin from "./pages/mypage/ChangePinPage.tsx";
import Transfer from "./pages/transfer/TransferPage.tsx";
import SendAmount from "./pages/transfer/SendAmountPage.tsx";
import { AuthContext } from "./context/AuthContext.tsx";
import ConfirmTransfer from "./pages/transfer/ConfirmTransferPage.tsx";
import TransferSuccess from "./pages/transfer/TransferSuccessPage.tsx";
import SettlementOptionsPage from "./pages/settlement/SettlementOptionsPage.tsx";
import EmailResult from "./pages/auth/EmailResultPage.tsx";
import AccountDetail from "./pages/transfer/AccountDetailPage.tsx";
import NotificationPage from "./pages/NotificationPage.tsx";
import { useContext, useEffect } from "react";
import { initializeInterceptors } from "./api/tacoApis.ts";
import useAuth from "./hooks/useAuth.ts";
import PrivateRoute from "./guard/PrivateRoute.tsx";
import PublicRoute from "./guard/PublicRoute.tsx";
import DeleteList from "./pages/friends/DeleteListPage.tsx";
import NotFoundPage from "./pages/error/NotFoundPage.tsx";
import ServerErrorPage from "./pages/error/ServerErrorPage.tsx";
import RequireMydataLinked from "./guard/RequireMydataLinked.tsx";

function App() {
  const { removeMember } = useAuth();
  const { isLoggedIn } = useContext(AuthContext);
  const hasVisitedSplash = localStorage.getItem("hasVisitedSplash") === "true";
  useEffect(() => {
    initializeInterceptors(removeMember); // removeMember 전달
  }, [removeMember]);
  // Check current domain and target domain from .env

  const targetDomain = import.meta.env.VITE_TEST_DOMAIN;
  const currentDomain = window.location.hostname;

  const isTargetDomain = currentDomain === targetDomain;

  const Watermark = ({ text }: { text: string }) => (
    <div
      style={{
        position: "fixed",
        top: 0,
        left: 0,
        width: "100%",
        height: "100%",
        pointerEvents: "none",
        background: "transparent",
        zIndex: 9999,
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        color: "rgba(0, 0, 0, 0.1)",
        fontSize: "5rem",
        fontWeight: "bold",
        textTransform: "uppercase",
      }}
    >
      {text}
    </div>
  );

  return (
    <div id="app-container">
      {isTargetDomain && <Watermark text="Test Test Test" />}
      <Routes>
        <Route path="404" element={<NotFoundPage />} />
        <Route path="500" element={<ServerErrorPage />} />
        <Route path="*" element={<Navigate to="/404" replace />} />

        <Route element={<PublicRoute />}>
          {!hasVisitedSplash && <Route path="/" element={<Splash />} />}
          {/* hasVisitedSplash가 true일 때 로그인 상태에 따라 렌더링 */}
          {hasVisitedSplash && !isLoggedIn && (
            <Route path="/" element={<Start />} />
          )}
          <Route path="/start" element={<Start />} />
          <Route path="/login" element={<Login />} />
          <Route path="/find-email" element={<FindEmail />} />
          <Route path="/email-result" element={<EmailResult />} />
          <Route path="/find-password" element={<FindPassword />} />
          <Route path="/join" element={<Join />} />
          <Route path="/reset-password" element={<ResetPassword />} />
        </Route>

        <Route element={<PrivateRoute />}>
          {hasVisitedSplash && <Route path="/" element={<Home />} />}
          <Route path="notice" element={<NotificationPage />} />

          <Route element={<RequireMydataLinked />}>
            <Route
              path="/account/detail/:accountId"
              element={<AccountDetail />}
            />
            <Route path="/settlement" element={<Settlement />}>
              <Route index element={<Navigate to="receive" replace />} />
              <Route path="receive" element={<ReceiveMoney />} />
              <Route path="details/:settlementId" element={<ReceiveDetail />} />
              <Route path="send" element={<SendMoney />} />
              <Route path="memberselect" element={<MemberSelect />} />
              <Route path="action" element={<SettlementOptionsPage />}>
                <Route path="split" element={<Split />} />
                <Route path="receipt" element={<Receipt />} />
              </Route>
              <Route path="request" element={<SettlementReq />} />
              <Route path="complete" element={<SettlementComplete />} />
            </Route>
            <Route path="/transfer" element={<Transfer />} />
            <Route path="/transfer/sendamount" element={<SendAmount />} />
            <Route path="/transfer/confirm" element={<ConfirmTransfer />} />
            <Route path="/transfer/success" element={<TransferSuccess />} />
          </Route>
          <Route path="/friends" element={<FriendList />} />
          <Route path="/blocklist" element={<BlockedList />} />
          <Route path="/deletedlist" element={<DeleteList />} />
          <Route path="/groups" element={<GroupList />} />
          <Route path="/addfriend" element={<AddFriend />} />
          <Route path="/create-group" element={<CreateGroup />} />
          <Route path="/groups/:groupId" element={<GroupDetail />} />

          <Route path="/my-info" element={<MyInfo />}>
            <Route path="change-password" element={<ChangePassword />} />
            <Route element={<RequireMydataLinked />}>
              <Route path="change-pin" element={<ChangePin />} />
            </Route>
          </Route>
        </Route>
      </Routes>
    </div>
  );
}

export default App;