package com.pantrymate.user.infrastructure.ai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pantrymate.user.domain.OnboardingPort;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AiOnboardingClient implements OnboardingPort {

    private static final Logger log = LoggerFactory.getLogger(AiOnboardingClient.class);
    private static final Map<String, String> CUISINE_CODES = Map.of(
            "KOREAN", "korean",
            "CHINESE", "chinese",
            "JAPANESE", "japanese",
            "WESTERN", "western",
            "ETC", "asian_other");

    private final RestClient restClient;
    private final String apiKey;

    public AiOnboardingClient(
            @Value("${ai.onboarding.base-url}") String baseUrl,
            @Value("${ai.onboarding.internal-api-key}") String apiKey,
            @Value("${ai.onboarding.timeout-ms}") long timeoutMs) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofMillis(500)).build());
        requestFactory.setReadTimeout(Duration.ofMillis(timeoutMs));
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
        this.apiKey = apiKey;
    }

    @Override
    public PresentedFoods getPresentedFoods() {
        try {
            AiPresented response = restClient
                    .get()
                    .uri("/v1/onboarding/presented")
                    .header("X-Internal-Api-Key", apiKey)
                    .exchange((req, res) -> {
                        int status = res.getStatusCode().value();
                        if (status != 200) {
                            throw new OnboardingUnavailableException("AI 온보딩 목록 응답 status=" + status);
                        }
                        return res.bodyTo(AiPresented.class);
                    });
            if (response == null || response.items() == null) {
                throw new OnboardingUnavailableException("AI 온보딩 목록 응답 본문이 비어 있음");
            }
            return new PresentedFoods(
                    response.listVersion(),
                    response.items().stream()
                            .map(item -> new Food(item.name(), item.family()))
                            .toList());
        } catch (OnboardingUnavailableException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new OnboardingUnavailableException("AI 온보딩 목록 호출 실패: " + e.getMessage());
        }
    }

    @Override
    public void saveOnboarding(OnboardingSnapshot snapshot) {
        boolean hasTaste = snapshot.spicy() != null && snapshot.salty() != null && snapshot.sweet() != null;
        AiOnboardingRequest request = new AiOnboardingRequest(
                snapshot.picks(),
                hasTaste ? new AiTaste(snapshot.spicy(), snapshot.salty(), snapshot.sweet()) : null,
                toCuisineCodes(snapshot.preferredFoodTypes()),
                snapshot.allergies(),
                snapshot.householdSize());
        try {
            AiOnboardingResponse response = restClient
                    .post()
                    .uri("/v1/onboarding/{userId}", snapshot.userId())
                    .header("X-Internal-Api-Key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .exchange((req, res) -> {
                        int status = res.getStatusCode().value();
                        if (status != 200) {
                            throw new OnboardingUnavailableException("AI 온보딩 저장 응답 status=" + status);
                        }
                        return res.bodyTo(AiOnboardingResponse.class);
                    });
            if (response != null
                    && response.unmappedAllergens() != null
                    && !response.unmappedAllergens().isEmpty()) {
                // 차단되지 않는 알레르기 라벨이 있다는 뜻이라 AI 팀과 라벨을 맞춰야 한다.
                log.warn(
                        "AI가 알레르기 라벨을 인식하지 못함 userId={} labels={}",
                        snapshot.userId(),
                        response.unmappedAllergens());
            }
        } catch (OnboardingUnavailableException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new OnboardingUnavailableException("AI 온보딩 저장 호출 실패: " + e.getMessage());
        }
    }

    private List<String> toCuisineCodes(List<String> preferredFoodTypes) {
        if (preferredFoodTypes == null || preferredFoodTypes.isEmpty()) {
            return null;
        }
        return preferredFoodTypes.stream().map(CUISINE_CODES::get).distinct().toList();
    }

    private record AiPresented(@JsonProperty("list_version") int listVersion, List<AiFood> items) {}

    private record AiFood(String name, String family) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record AiOnboardingRequest(
            List<String> picks,
            @JsonProperty("taste_preferences") AiTaste tastePreferences,
            @JsonProperty("preferred_cuisines") List<String> preferredCuisines,
            @JsonProperty("allergy_groups") List<String> allergyGroups,
            @JsonProperty("household_size") Integer householdSize) {}

    private record AiTaste(int spicy, int salty, int sweet) {}

    private record AiOnboardingResponse(@JsonProperty("unmapped_allergens") List<String> unmappedAllergens) {}
}
