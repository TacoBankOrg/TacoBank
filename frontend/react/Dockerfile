# Stage 1: React 빌드
FROM node:18 AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Stage 2: NGINX 설정 및 정적 파일 서빙
FROM nginx:alpine
COPY nginx.conf /etc/nginx/nginx.conf 
COPY --from=build /app/dist /usr/share/nginx/taco 

EXPOSE 9003
CMD ["nginx", "-g", "daemon off;"]