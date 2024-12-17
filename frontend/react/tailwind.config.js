/** @type {import('tailwindcss').Config} */
const generateGrayscaleKeyframes = () => {
  const keyframes = {};
  for (let i = 0; i <= 100; i += 1) {
    keyframes[`${i}%`] = { filter: `grayscale(${100 - i}%)` };
  }
  return keyframes;
};

export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      fontFamily: {
        sans: ['"Pretendard Variable"', 'sans-serif'], // Pretendard Variable 추가
      },
      colors: {
        background: "var(--background)",
        foreground: "var(--foreground)",
      },
      keyframes: {
        grayscaleToColor: generateGrayscaleKeyframes(),
      },
      animation: {
        grayscaleToColor: "grayscaleToColor 1s infinite",
      },
    },
  },
  plugins: [],
};