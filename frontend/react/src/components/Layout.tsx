import React from "react";
import BottomNav from "./BottomNav";

const Layout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return (
    <div className="layout-container">
      <main className="main-content">{children}</main>
      <footer className="footer">
        <BottomNav />
      </footer>
    </div>
  );
};

export default Layout;