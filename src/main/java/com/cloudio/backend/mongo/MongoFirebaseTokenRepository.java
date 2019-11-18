package com.cloudio.backend.mongo;

import com.cloudio.backend.repository.FirebaseTokenRepository;
import com.mongodb.client.MongoDatabase;
import com.cloudio.backend.model.FirebaseToken;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository

public class MongoFirebaseTokenRepository implements FirebaseTokenRepository {

    @Autowired
    private MongoDb mongoDb;

    public MongoFirebaseTokenRepository() {

    }

    @Override
    public Optional<Boolean> removeByAccountId(String accountId) {
        return Optional.of(mongoDb.getMongoDatabase().getCollection(FirebaseToken.COLLECTION_NAME, FirebaseToken.class)
                .deleteOne(new Document("accountId", accountId)).getDeletedCount() == 1);
    }
}
