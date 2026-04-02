package com.n4d3sh1k4.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}


	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder, AuthenticationGatewayFilterFactory authFilter) {
		return builder.routes()
				// Маршрут 1: Регистрация, логин, восстановление пароля(без фильтра)
				.route("auth-public", r -> r.path(
						"/api/v0/auth/login",
						"/api/v0/auth/register",
						"/api/v0/auth/refresh",
						"/api/v0/auth/forgot-password",
						"/api/v0/auth/reset-password",
						"/api/v0/auth/confirm",
						"/api/v0/auth/resend-confirmation",
						"/api/v0/oauth2/**",
						"/api/v0//login/oauth2/**"
				).uri("lb://security-service"))

				.route("auth-logout", r -> r.path("/api/v0/auth/logout")
						.filters(f -> f.filter(authFilter.apply(new AuthenticationGatewayFilterFactory.Config())))
						.uri("lb://security-service"))

				// Маршрут 2: Статус (с фильтром)
				.route("auth-status", r -> r.path("/api/v0/status/**")
						.filters(f -> f.filter(authFilter.apply(new AuthenticationGatewayFilterFactory.Config())))
						.uri("lb://security-service"))

				// Маршрут 3: Работа с данными пользователей
				.route("user-service", r -> r.path("/api/v0/user/**")
						.filters(f -> f.filter(authFilter.apply(new AuthenticationGatewayFilterFactory.Config())))
						.uri("lb://user-service"))
				.build();


	}
}
