# Node.js 기반 프론트엔드 서버 배포
FROM node:18-slim
WORKDIR /app

# 의존성 설치 및 코드 복사
COPY package.json package-lock.json ./
RUN npm install
COPY . .

# 빌드 및 프론트엔드 서버 실행
RUN npm run build
EXPOSE 5173

CMD ["npm", "run", "preview"]
