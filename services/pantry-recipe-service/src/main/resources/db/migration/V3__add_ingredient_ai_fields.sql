-- AI 추천 엔진 연동을 위한 재료 사전 보강 컬럼
-- is_staple: 상비 재료 여부(간장·소금 등 팬트리 매칭에서 제외할 재료)
-- allergens: 재료가 유발할 수 있는 알레르기 코드 배열(한 재료가 여러 알레르기 군에 속할 수 있음, 예: 간장 = 대두+밀)
ALTER TABLE ingredients
    ADD COLUMN is_staple BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN allergens TEXT[] NOT NULL DEFAULT '{}';
