interface CheckIconProps {
  isActive: boolean; // 조건 충족 여부
}

export default function CheckIcon({ isActive }: CheckIconProps) {
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      fill="none"
      viewBox="0 0 24 24"
      strokeWidth={1}
      stroke={isActive ? "#22c55e" : "gray"} // 충족 시 초록색, 미충족 시 회색
      className="w-3 h-3"
    >
      <path
        strokeLinecap="round"
        strokeLinejoin="round"
        d="M9 12.75 11.25 15 15 9.75M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z"
      />
    </svg>
  );
}
