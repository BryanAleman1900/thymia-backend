package com.project.demo.logic.service.calendar;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class GoogleCalendarService {

    private static final String APPLICATION_NAME = "Thymia App";
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private Calendar service;

    public GoogleCalendarService() throws Exception {
        InputStream in = getClass().getResourceAsStream("/credentials.json");
        var creds = ServiceAccountCredentials.fromStream(in)
                .createScoped("https://www.googleapis.com/auth/calendar.events")
                .createDelegated("usuario@tu-dominio.com");

        service = new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                new HttpCredentialsAdapter(creds)
        )
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public Event createEvent(String calendarId, Event event) throws Exception {
        return service.events().insert(calendarId, event).execute();
    }
}

