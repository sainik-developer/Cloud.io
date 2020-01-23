package com.cloudio.rest.service;

import com.cloudio.rest.exception.FirebaseException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Log4j2
@Service
@RequiredArgsConstructor
public class FirebaseService {

    private final AWSS3Services awss3Services;
    @Value("${amazonProperties.sensitive.bucketName}")
    private String bucketName;
    @Value("${amazonProperties.sensitive.fileName}")
    private String fileName;
    @Value("${firebase.database.url}")
    private String firebaseDataBaseUrl;

    @PostConstruct
    public void init() {
        try {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(awss3Services.getFile(bucketName, fileName).getObjectContent()))
                    .setDatabaseUrl(firebaseDataBaseUrl)
                    .build();
            FirebaseApp.initializeApp(options);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }


    public String refreshFireBaseCustomToken(final String accountId) {
        try {
            return FirebaseAuth.getInstance().createCustomToken(accountId);
        } catch (final FirebaseAuthException e) {
            throw new FirebaseException(e.getMessage());
        }
    }

    public boolean revokeFireBaseCustomToken(final String accountId) {
        try {
            FirebaseAuth.getInstance().revokeRefreshTokens(accountId);
            return true;
        } catch (final FirebaseAuthException e) {
            return false;
        }
    }
}
