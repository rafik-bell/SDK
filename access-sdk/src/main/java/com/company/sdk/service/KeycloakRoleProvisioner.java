package com.company.sdk.service;

import com.company.sdk.model.AccessRulesConfig;
import com.company.sdk.model.RoleDefinition;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class KeycloakRoleProvisioner {

    private static final String MANAGED_BY_ATTRIBUTE = "managed-by";
    private static final String LEGACY_DESCRIPTION_PREFIX = "Role created by ";

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
            // Full representation so role attributes (ownership marker) are included
            List<RoleRepresentation> existingRoles = rolesResource.list(false);

            List<RoleDefinition> declaredRoles = config.getRoles() != null ? config.getRoles() : Collections.emptyList();
            String serviceName = config.getServiceName();
            String defaultDescription = LEGACY_DESCRIPTION_PREFIX + serviceName;

            // 1. CREATE MISSING ROLES / UPDATE CHANGED ONES
            for (RoleDefinition role : declaredRoles) {
                String roleName = role.getName();
                String description = role.getDescription() != null ? role.getDescription() : defaultDescription;

                RoleRepresentation existing = existingRoles.stream()
                        .filter(r -> r.getName().equalsIgnoreCase(roleName))
                        .findFirst()
                        .orElse(null);

                if (existing == null) {
                    RoleRepresentation newRole = new RoleRepresentation();
                    newRole.setName(roleName);
                    newRole.setDescription(description);
                    newRole.setComposite(role.isComposite());
                    newRole.setAttributes(Map.of(MANAGED_BY_ATTRIBUTE, List.of(serviceName)));
                    rolesResource.create(newRole);
                    System.out.println("[SDK] Role created in Keycloak: " + roleName);
                } else if (isManagedBy(existing, serviceName)) {
                    boolean changed = !description.equals(existing.getDescription())
                            || !List.of(serviceName).equals(attributes(existing).get(MANAGED_BY_ATTRIBUTE));

                    if (changed) {
                        Map<String, List<String>> attrs = new HashMap<>(attributes(existing));
                        attrs.put(MANAGED_BY_ATTRIBUTE, List.of(serviceName));
                        existing.setDescription(description);
                        existing.setAttributes(attrs);
                        rolesResource.get(existing.getName()).update(existing);
                        System.out.println("[SDK] Role updated in Keycloak: " + roleName);
                    } else {
                        System.out.println("[SDK] Role already exists: " + roleName);
                    }
                } else {
                    System.out.println("[SDK] Role already exists (not managed by " + serviceName + "): " + roleName);
                }
            }

            // Case-insensitive set of declared roles for fast lookup
            Set<String> declaredRoleNames = declaredRoles.stream()
                    .map(r -> r.getName().toLowerCase())
                    .collect(Collectors.toSet());

            // 2. DELETE ROLES REMOVED FROM JSON
            for (RoleRepresentation existingRole : existingRoles) {
                // Safety Guard: Only delete if the role belongs to this specific service
                if (isManagedBy(existingRole, serviceName)
                        && !declaredRoleNames.contains(existingRole.getName().toLowerCase())) {
                    rolesResource.deleteRole(existingRole.getName());
                    System.out.println("[SDK] Role removed from JSON, deleted from Keycloak: " + existingRole.getName());
                }
            }

        } catch (Exception e) {
            System.err.println("[SDK] Error syncing roles: " + e.getMessage());
        }
    }

    private static boolean isManagedBy(RoleRepresentation role, String serviceName) {
        List<String> managedBy = attributes(role).get(MANAGED_BY_ATTRIBUTE);
        if (managedBy != null) {
            return managedBy.contains(serviceName);
        }
        // Roles created before the managed-by attribute existed
        return (LEGACY_DESCRIPTION_PREFIX + serviceName).equals(role.getDescription());
    }

    private static Map<String, List<String>> attributes(RoleRepresentation role) {
        return role.getAttributes() != null ? role.getAttributes() : Collections.emptyMap();
    }
}
