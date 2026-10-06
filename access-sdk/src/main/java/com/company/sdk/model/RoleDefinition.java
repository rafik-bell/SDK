package com.company.sdk.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public class RoleDefinition {
    private String name;
    private String description;
    private boolean composite;

    public RoleDefinition() {}

    // Supports the legacy format where a role is just its name: "roles": ["my_role"]
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static RoleDefinition fromName(String name) {
        RoleDefinition role = new RoleDefinition();
        role.setName(name);
        return role;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isComposite() { return composite; }
    public void setComposite(boolean composite) { this.composite = composite; }
}
