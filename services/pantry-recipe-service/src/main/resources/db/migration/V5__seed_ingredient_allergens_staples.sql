-- 식재료 사전의 상비 재료(is_staple)와 알레르기 군(allergens) 초안 시딩.
-- allergens 값은 온보딩 알레르기 선택 라벨(19종)과 동일한 한글 라벨을 쓴다.
-- 알레르기 군의 범위는 AI 명세 5.2 표(군 전체 차단 원칙)를 따르고, 간장·고추장·된장·굴소스·카레처럼
-- 여러 군이 겹치는 가공 재료는 함께 표기한다. 표에 대응 라벨이 없는 군(참깨·메밀·복숭아·아황산류)은 표기하지 않는다.

-- 상비 재료: 대부분의 가정에 늘 있는 양념·기름류
UPDATE ingredients SET is_staple = TRUE
WHERE name IN ('소금', '설탕', '간장', '식용유', '후추', '참기름', '식초', '물엿', '올리고당', '맛술',
               '고춧가루', '들기름', '올리브유', '통깨', '고추장', '된장', '굴소스', '액젓', '전분');

-- 알류(가금류)
UPDATE ingredients SET allergens = ARRAY['알류(가금류)'] WHERE name IN ('달걀', '메추리알', '마요네즈');

-- 우유
UPDATE ingredients SET allergens = ARRAY['우유']
WHERE name IN ('우유', '버터', '생크림', '연유', '요거트', '치즈', '모짜렐라치즈', '파마산치즈', '크림치즈', '초콜릿');

-- 대두
UPDATE ingredients SET allergens = ARRAY['대두'] WHERE name IN ('두부', '콩나물');
UPDATE ingredients SET allergens = ARRAY['대두', '밀'] WHERE name IN ('간장', '고추장', '된장');
UPDATE ingredients SET allergens = ARRAY['조개류(굴,전복,홍합 포함)', '대두', '밀'] WHERE name = '굴소스';

-- 밀
UPDATE ingredients SET allergens = ARRAY['밀']
WHERE name IN ('밀가루', '식빵', '파스타면', '소면', '라면', '부침가루', '튀김가루', '빵가루', '만두', '또띠아',
               '돈가스소스', '카레');

-- 견과
UPDATE ingredients SET allergens = ARRAY['땅콩'] WHERE name = '땅콩';
UPDATE ingredients SET allergens = ARRAY['호두'] WHERE name = '호두';
UPDATE ingredients SET allergens = ARRAY['땅콩', '호두', '잣'] WHERE name IN ('아몬드', '견과류');

-- 생선류 (AI 군 기준: 고등어 라벨로 어류 전체 차단)
UPDATE ingredients SET allergens = ARRAY['고등어']
WHERE name IN ('고등어', '멸치', '참치', '어묵', '액젓', '맛살', '북어', '명란젓', '참치액');

-- 새우·조개류
UPDATE ingredients SET allergens = ARRAY['새우'] WHERE name IN ('새우', '건새우', '새우젓', '김치');
UPDATE ingredients SET allergens = ARRAY['조개류(굴,전복,홍합 포함)'] WHERE name IN ('바지락', '굴');

-- 오징어
UPDATE ingredients SET allergens = ARRAY['오징어'] WHERE name IN ('오징어', '진미채');

-- 육류
UPDATE ingredients SET allergens = ARRAY['돼지고기'] WHERE name IN ('돼지고기', '베이컨', '햄', '소시지', '스팸');
UPDATE ingredients SET allergens = ARRAY['쇠고기'] WHERE name = '소고기';
UPDATE ingredients SET allergens = ARRAY['닭고기'] WHERE name = '닭고기';

-- 토마토
UPDATE ingredients SET allergens = ARRAY['토마토'] WHERE name IN ('토마토', '케첩', '토마토소스');
