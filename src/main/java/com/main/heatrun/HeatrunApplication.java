package com.main.heatrun;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableConfigurationProperties  // JwtProperties 활성화
@EnableJpaAuditing  // createdAt, updatedAt 자동 관리
@SpringBootApplication
public class HeatrunApplication {

	public static void main(String[] args) {
		SpringApplication.run(HeatrunApplication.class, args);
	}

}
