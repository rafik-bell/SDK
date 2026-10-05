package com.company.sdk.model;

import java.util.List;

public class AccessRulesConfig {
    private String serviceName;
    private List<String> roles;

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}
