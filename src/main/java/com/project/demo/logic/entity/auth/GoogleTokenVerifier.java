package com.project.demo.logic.entity.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;

public class GoogleTokenVerifier {

    private static final String CLIENT_ID = "82191624415-m9dki2mc78iuloiig6oo6cn0s8mg1sf6.apps.googleusercontent.com";

    private static final GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
            new NetHttpTransport(), new GsonFactory())
            .setAudience(Collections.singletonList(CLIENT_ID))
            .build();

    public static Optional<GoogleIdToken.Payload> verify(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                return Optional.of(idToken.getPayload());
            }
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
