package com.company.sdk.service;

import com.company.sdk.model.AccessRulesConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class KeycloakRoleProvisioner {

    private final Keycloak keycloak;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public KeycloakRoleProvisioner(Keycloak keycloak, ResourceLoader resourceLoader) {
        this.keycloak = keycloak;
        this.resourceLoader = resourceLoader;
    }

    public void syncRolesFromFile(String filePath, String targetRealm) {
        try {
            Resource resource = resourceLoader.getResource(filePath);
            if (!resource.exists()) {
                System.err.println("File not found: " + filePath);
                return;
            }

            AccessRulesConfig config;
            try (InputStream is = resource.getInputStream()) {
                config = objectMapper.readValue(is, AccessRulesConfig.class);
            }

            RealmResource realmResource = keycloak.realm(targetRealm);
            RolesResource rolesResource = realmResource.roles();
            List<RoleRepresentation> existingRoles = rolesResource.list();

            List<String> declaredRoles = config.getRoles() != null ? config.getRoles() : Collections.emptyList();
            String managedDescription = "Role created by " + config.getServiceName();

            // 1. CREATE MISSING ROLES
            for (String roleName : declaredRoles) {
                boolean exists = existingRoles.stream()
                        .anyMatch(r -> r.getName().equalsIgnoreCase(roleName));

                if (!exists) {
                    RoleRepresentation newRole = new RoleRepresentation();
                    newRole.setName(roleName);
                    newRole.setDescription(managedDescription);
                    rolesResource.create(newRole);
                    System.out.println("[SDK] Role created in Keycloak: " + roleName);
                } else {
                    System.out.println("[SDK] Role already exists: " + roleName);
                }
            }

            // Case-insensitive set of declared roles for fast lookup
            Set<String> declaredRoleNames = declaredRoles.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());

            // 2. DELETE ROLES REMOVED FROM JSON
            for (RoleRepresentation existingRole : existingRoles) {
                // Safety Guard: Only delete if the role belongs to this specific service
                boolean isManagedByThisService = managedDescription.equals(existingRole.getDescription());

                if (isManagedByThisService && !declaredRoleNames.contains(existingRole.getName().toLowerCase())) {
                    rolesResource.deleteRole(existingRole.getName());
                    System.out.println("[SDK] Role removed from JSON, deleted from Keycloak: " + existingRole.getName());
                }
            }

        } catch (Exception e) {
            System.err.println("[SDK] Error syncing roles: " + e.getMessage());
        }
    }
}