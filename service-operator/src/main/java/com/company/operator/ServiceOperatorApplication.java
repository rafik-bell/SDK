package com.company.operator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.company.operator", "com.company.sdk"})
public class ServiceOperatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceOperatorApplication.class, args);
    }
}

