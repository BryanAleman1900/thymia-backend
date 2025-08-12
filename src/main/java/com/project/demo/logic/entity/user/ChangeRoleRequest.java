package com.project.demo.logic.entity.user;

public class ChangeRoleRequest {
    private String role; // acepta "USER" o "THERAPIST"

    public ChangeRoleRequest() {}

    public ChangeRoleRequest(String role) {
        this.role = role;
    }

    public String getRole() { return role; }

    public void setRole(String role) { this.role = role; }
}
