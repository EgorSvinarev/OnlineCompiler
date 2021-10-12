package com.svinarev.compiler.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI customApi() {
		return new OpenAPI()
				.info(
					new Info()
						.title("Online compiler API")
						.version("1.0.0")
						.contact(
								new Contact()
									.email("egor-rdd@mail.ru")
									.name("Svinarev Egor")
						)
				);
	}
	
}
