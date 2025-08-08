package com.project.demo.logic.entity.session;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionStore {
    // roomId -> password
    private final Map<String, String> rooms = new ConcurrentHashMap<>();

    public void put(String roomId, String password) { rooms.put(roomId, password); }
    public String getPassword(String roomId) { return rooms.get(roomId); }
    public boolean exists(String roomId) { return rooms.containsKey(roomId); }
}
