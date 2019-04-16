package lt.bit.webservices;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.calendar.model.Event;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;


public class GoogleAuthService {

    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";


    private NetHttpTransport HTTP_TRANSPORT;
    private GoogleAuthorizationCodeFlow flow;
    private LocalServerReceiver receiver;

    private String authorizationUrl;
    private String redirectUrl;

//    private GoogleAuthService() {
//
//    }

//    public GoogleAuthService getIns


    public GoogleAuthService(String userId) {
        try {
            this.authorizationUrl = authorizationUrl(userId);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }


    public List<Event> getUserEvents(String userId) throws IOException {
        Calendar service = new Calendar.Builder(this.HTTP_TRANSPORT, JSON_FACTORY, getCredentials(userId))
                .setApplicationName(APPLICATION_NAME)
                .build();

        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = service.events().list("primary")
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();

        return items;
    }

    public void waitForCode(String userId) throws IOException {
        try {
            String code = this.receiver.waitForCode();
            TokenResponse response = flow.newTokenRequest(code).setRedirectUri(this.redirectUrl).execute();

            this.flow.createAndStoreCredential(response, userId);
        } finally {
            this.receiver.stop();
        }
    }

    public boolean isAuthorized(String userId) {
        Credential credential = null;
        try {
            credential = flow.loadCredential(userId);
        } catch (IOException e) {
            return false;
        }

        return credential != null;
    }

    public String getAuthorizationUrl() {
        return authorizationUrl;
    }

    private Credential getCredentials(String userId) {
        try {
            Credential credential = flow.loadCredential(userId);
            if (credential != null
                    && (credential.getRefreshToken() != null ||
                    credential.getExpiresInSeconds() == null ||
                    credential.getExpiresInSeconds() > 60)) {
                return credential;
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    private String authorizationUrl(String userId) throws IOException, GeneralSecurityException {
        this.HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        // Load client secrets.
        InputStream in = GoogleAuthService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        this.flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        this.receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        this.redirectUrl = this.receiver.getRedirectUri();
        String authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(this.redirectUrl).build();

        return authorizationUrl;
    }


}
