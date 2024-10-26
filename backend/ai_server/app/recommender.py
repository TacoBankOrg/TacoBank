import random

def recommend_products(profile: dict):
    products = [
        {"product_name": "안전 채권", "score": random.uniform(0, 1)},
        {"product_name": "위험 펀드", "score": random.uniform(0, 1)},
        {"product_name": "고위험 주식", "score": random.uniform(0, 1)},
    ]
    return sorted(products, key=lambda x: -x["score"])
