package com.pantrymate.pantryrecipe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.pantrymate")
@EnableFeignClients
public class PantryRecipeApplication {

    public static void main(String[] args) {
        SpringApplication.run(PantryRecipeApplication.class, args);
    }
}
