package com.capstone.disc_persona_chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.capstone.disc_persona_chat.config.properties.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class DiscPersonaChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscPersonaChatApplication.class, args);
    }

}

