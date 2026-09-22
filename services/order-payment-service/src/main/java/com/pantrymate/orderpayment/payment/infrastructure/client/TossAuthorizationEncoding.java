package com.pantrymate.orderpayment.payment.infrastructure.client;

import com.pantrymate.orderpayment.payment.application.dto.TossConfirmRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TossAuthorizationEncoding {

    @Value("${toss.secret-key}")
    private String secretKey;

    public String createAuthorization() {
        String encoded = Base64.getEncoder()
            .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

}
