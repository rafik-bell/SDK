package com.company.sdk.model;

import java.util.List;

public class AccessRulesConfig {
    private String serviceName;
    private String version;
    private String description;
    private List<RoleDefinition> roles;

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<RoleDefinition> getRoles() { return roles; }
    public void setRoles(List<RoleDefinition> roles) { this.roles = roles; }
}
