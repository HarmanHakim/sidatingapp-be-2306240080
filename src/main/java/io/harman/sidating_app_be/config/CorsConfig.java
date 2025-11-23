package io.harman.sidating_app_be.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")
						.allowedOrigins(
							"http://localhost:5173",
							"http://localhost:3000",
							"http://localhost:8080",
							"http://localhost:80",
							"http://2306240080-sidating-fe.hafizmuh.site",
							"http://2306240080-sidating-be1.hafizmuh.site",
							"http://2306240080-sidating-be2.hafizmuh.site"
						)
						.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
						.allowedHeaders("*")
						.allowCredentials(true)
						.exposedHeaders("Authorization")
						.maxAge(3600);
			}
		};
	}
}