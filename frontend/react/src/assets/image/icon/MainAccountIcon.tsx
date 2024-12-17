import React from "react";

interface StarIconProps {
  filled?: boolean; // true일 경우 채워진 별, false일 경우 비워진 별
  size?: number; // 아이콘 크기 (기본값: 24)
  className?: string; // 추가적인 클래스 이름
}

const MainAccountIcon: React.FC<StarIconProps> = ({
  filled = false,
  size = 24, // 기본 크기 24px
  className = "",
}) => {
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      fill={filled ? "#FDBF0F" : "#BDBDBD"} // 채워진 상태: 노랑색
      viewBox="0 0 24 24"
      strokeWidth={1.0}
      stroke={filled ? "#FDBF0F" : "#BDBDBD"} // 비워진 상태: 회색
      style={{ width: `${size}px`, height: `${size}px` }} // 동적 크기 설정
      className={className}
    >
      <path
        strokeLinecap="round"
        strokeLinejoin="round"
        d="M11.48 3.499a.562.562 0 0 1 1.04 0l2.125 5.111a.563.563 0 0 0 .475.345l5.518.442c.499.04.701.663.321.988l-4.204 3.602a.563.563 0 0 0-.182.557l1.285 5.385a.562.562 0 0 1-.84.61l-4.725-2.885a.562.562 0 0 0-.586 0L6.982 20.54a.562.562 0 0 1-.84-.61l1.285-5.386a.562.562 0 0 0-.182-.557l-4.204-3.602a.562.562 0 0 1 .321-.988l5.518-.442a.563.563 0 0 0 .475-.345L11.48 3.5Z"
      />
    </svg>
  );
};

export default MainAccountIcon;