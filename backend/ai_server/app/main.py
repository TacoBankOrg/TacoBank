from fastapi import FastAPI
from app.models import UserProfile, RecommendationResponse
from app.recommender import recommend_products

# 실행
# uvicorn app.main:app --reload --host 0.0.0.0 --port 8082


# FastAPI 애플리케이션 생성
app = FastAPI()

# /recommend 엔드포인트 정의
@app.post("/recommend", response_model=list[RecommendationResponse])
async def get_recommendations(profile: UserProfile):
    """
    사용자 프로필을 받아 추천 상품 목록을 반환하는 API.
    """
    recommendations = recommend_products(profile.dict())
    return recommendations
