package edu.neu.info7255.ECommerceApplication.service;

import java.util.Collections;

import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

@Service
public class AuthService {
    private static final JacksonFactory jacksonFactory = new JacksonFactory();

    private String GOOGLE_CLIENT_ID = "407408718192.apps.googleusercontent.com";

    GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new ApacheHttpTransport(), jacksonFactory)
            .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID)).build();

    public boolean authorize(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);

            if(idToken != null) return true;

            return false;

        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
