# 1. Python 베이스 이미지 사용
FROM python:3.9-slim

# 2. 작업 디렉터리 설정
WORKDIR /backend/ai_server

# 3. 필요한 파일 복사 및 의존성 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 4. 전체 프로젝트 복사
COPY . .

# 5. FastAPI 서버 실행 (모듈 경로 명확히 지정)
EXPOSE 8082
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "80823", "--reload"]
