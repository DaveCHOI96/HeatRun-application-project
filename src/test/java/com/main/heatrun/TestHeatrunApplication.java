package com.main.heatrun;

import org.springframework.boot.SpringApplication;

public class TestHeatrunApplication {

	public static void main(String[] args) {
		SpringApplication.from(HeatrunApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
