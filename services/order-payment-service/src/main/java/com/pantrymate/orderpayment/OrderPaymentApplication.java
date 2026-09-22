package com.pantrymate.orderpayment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = "com.pantrymate")
@EnableJpaAuditing
@EnableFeignClients
public class OrderPaymentApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderPaymentApplication.class, args);
    }
}
