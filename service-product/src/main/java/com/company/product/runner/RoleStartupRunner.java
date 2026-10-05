package com.company.product.runner;

import com.company.sdk.service.KeycloakRoleProvisioner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RoleStartupRunner implements CommandLineRunner {

    private final KeycloakRoleProvisioner roleProvisioner;

    @Value("${keycloak.realm}")
    private String realm;

    public RoleStartupRunner(KeycloakRoleProvisioner roleProvisioner) {
        this.roleProvisioner = roleProvisioner;
    }

    @Override
    public void run(String... args) {
        roleProvisioner.syncRolesFromFile("classpath:access-rules.json", realm);
    }
}
