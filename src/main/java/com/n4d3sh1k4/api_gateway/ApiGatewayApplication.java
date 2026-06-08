package com.n4d3sh1k4.api_gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class ApiGatewayApplication {

    @Value("${services.security-service.uri}")
    private String securityServiceUri;

    @Value("${services.solution-archive-service.uri}")
    private String solutionServiceUri;

    @Value("${services.dozzle.uri}")
    private String dozzleUri;


    @Value("${rabbitmq.admin.uri}")
    private String rabbitmqUri;

    @Value("${minio.admin.uri}")
    private String minioUri;

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, AuthenticationGatewayFilterFactory authFilter, AdminAuthenticationGatewayFilterFactory adminAuthFilter) {

        final String API_PREFIX = "/api/v0";

        return builder.routes()
                .route("security-service-public", r -> r
                        .path(
                                API_PREFIX + "/auth/login",
                                API_PREFIX + "/auth/register",
                                API_PREFIX + "/auth/refresh",
                                API_PREFIX + "/auth/forgot-password",
                                API_PREFIX + "/auth/reset-password",
                                API_PREFIX + "/auth/confirm-email",
                                API_PREFIX + "/auth/resend-confirmation",
                                API_PREFIX + "/auth/yandex-mobile",
                                API_PREFIX + "/auth/link-social",
                                API_PREFIX + "/oauth2/**",
                                API_PREFIX + "/login/oauth2/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri(securityServiceUri))

                .route("security-service-private", r -> r
                        .path(API_PREFIX + "/auth/logout",
                                API_PREFIX + "/user",
                                API_PREFIX + "/user/*",
                                API_PREFIX + "/status/hello")
                        .filters(f -> f
                                .filter(authFilter.apply(new AuthenticationGatewayFilterFactory.Config()))
                                .stripPrefix(2))
                        .uri(securityServiceUri))

                .route("solution-archive-private", r -> r
                        .path(API_PREFIX + "/equation/recognition/**",
                                API_PREFIX + "/equation/user/**",
                                API_PREFIX + "/equation/dataset"
                        )
                        .filters(f -> f
                                .filter(authFilter.apply(new AuthenticationGatewayFilterFactory.Config()))
                                .stripPrefix(2))
                        .uri(solutionServiceUri))

                //Admin Part
                .route("rabbitmq-ui-route", r -> r
                        .path("/admin/rabbit/**")
                        .filters(f -> f
                                .filter(adminAuthFilter.apply(new AdminAuthenticationGatewayFilterFactory.Config()))
                                .preserveHostHeader()
                                .removeResponseHeader("X-Frame-Options")
                                .removeResponseHeader("Content-Security-Policy"))
                        .uri(rabbitmqUri))

                .route("minio-ui-route", r -> r
                        .path("/admin/minio/**")
                        .filters(f -> f
                                .filter(adminAuthFilter.apply(new AdminAuthenticationGatewayFilterFactory.Config()))
                                .preserveHostHeader()
                                .stripPrefix(2)
                                .removeResponseHeader("X-Frame-Options")
                                .removeResponseHeader("Content-Security-Policy"))
                        .uri(minioUri))

                .route("swagger-config-default-pass", r -> r
                        .path("/v3/api-docs/swagger-config")
                        .filters(f -> f
                                .filter(adminAuthFilter.apply(new AdminAuthenticationGatewayFilterFactory.Config())))
                        .uri(securityServiceUri))

                .route("swagger-docs-security", r -> r
                        .path("/admin/api-docs/security-service")
                        .filters(f -> f
                                .filter(adminAuthFilter.apply(new AdminAuthenticationGatewayFilterFactory.Config()))
                                .rewritePath("/admin/api-docs/security-service", "/v3/api-docs"))
                        .uri(securityServiceUri))

                .route("swagger-docs-solution", r -> r
                        .path("/admin/api-docs/solution-archive-service")
                        .filters(f -> f
                                .filter(adminAuthFilter.apply(new AdminAuthenticationGatewayFilterFactory.Config()))
                                .rewritePath("/admin/api-docs/solution-archive-service", "/v3/api-docs"))
                        .uri(solutionServiceUri))

                .route("swagger-ui-integrated", r -> r
                        .path("/admin/swagger/**")
                        .filters(f -> f
                                .filter(adminAuthFilter.apply(new AdminAuthenticationGatewayFilterFactory.Config()))
                                .rewritePath("/admin/swagger/(?<segment>.*)", "/swagger-ui/${segment}")
                                .removeResponseHeader("X-Frame-Options")
                                .removeResponseHeader("Content-Security-Policy"))
                        .uri(securityServiceUri))

                .route("dozzle-monitoring", r -> r
                        .path("/admin/monitoring/**")
                        .filters(f -> f
                                .filter(adminAuthFilter.apply(new AdminAuthenticationGatewayFilterFactory.Config()))
                                .removeResponseHeader("X-Frame-Options")
                                .removeResponseHeader("Content-Security-Policy"))
                        .uri(dozzleUri))
                .build();
    }
}