package com.project.demo.logic.entity.session;

public class JoinSessionResponse {
    private String roomId;
    private String password;

    public JoinSessionResponse() {}

    public JoinSessionResponse(String roomId, String password) {
        this.roomId = roomId;
        this.password = password;
    }

    public String getRoomId() { return roomId; }
    public String getPassword() { return password; }
}
