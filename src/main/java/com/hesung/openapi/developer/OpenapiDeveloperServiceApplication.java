package com.hesung.openapi.developer;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableApolloConfig
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "com.hesung")
public class OpenapiDeveloperServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenapiDeveloperServiceApplication.class, args);
    }
}
