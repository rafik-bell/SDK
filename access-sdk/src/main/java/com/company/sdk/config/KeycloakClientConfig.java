package com.company.sdk.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakClientConfig {

    @Value("${keycloak.server-url:http://localhost:8080}")
    private String serverUrl;

    @Value("${keycloak.realm:master}")
    private String realm;

    @Value("${keycloak.client-id:admin-cli}")
    private String clientId;

    @Value("${keycloak.client-secret:}")
    private String clientSecret;

    @Value("${keycloak.username:admin}")
    private String username;

    @Value("${keycloak.password:admin}")
    private String password;

    @Bean
    public Keycloak keycloak() {
        KeycloakBuilder builder = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId(clientId);

        if (clientSecret != null && !clientSecret.isEmpty()) {
            builder.grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                   .clientSecret(clientSecret);
        } else {
            builder.grantType(OAuth2Constants.PASSWORD)
                   .username(username)
                   .password(password);
        }

        return builder.build();
    }
}
