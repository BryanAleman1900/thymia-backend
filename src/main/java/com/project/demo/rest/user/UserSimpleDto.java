package com.project.demo.rest.user;

public class UserSimpleDto {
    private Long id;
    private String fullName;
    private String email;

    public UserSimpleDto(Long id, String fullName, String email) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public String getFullName() {return fullName;}
}
