package com.project.demo.logic.entity.session;

public class CreateSessionResponse {
    private String roomId;
    private String password;

    public CreateSessionResponse() {}

    public CreateSessionResponse(String roomId, String password) {
        this.roomId = roomId;
        this.password = password;
    }

    public String getRoomId() { return roomId; }
    public String getPassword() { return password; }
}
