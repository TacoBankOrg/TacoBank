from pydantic import BaseModel

class UserProfile(BaseModel):
    age: int
    income: float
    investment_preference: str  # 예: 'low', 'medium', 'high'

class RecommendationResponse(BaseModel):
    product_name: str
    score: float
