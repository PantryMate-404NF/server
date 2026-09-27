package com.pantrymate.pantryrecipe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.pantrymate")
@EnableScheduling
@EnableFeignClients
public class PantryRecipeApplication {

    public static void main(String[] args) {
        SpringApplication.run(PantryRecipeApplication.class, args);
    }
}
