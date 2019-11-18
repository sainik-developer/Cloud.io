package com.cloudio.backend.mongo;

import com.cloudio.backend.repository.AccessTokenRepository;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.cloudio.backend.model.AccessToken;
import lombok.extern.log4j.Log4j2;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Log4j2
@Repository
public  class MongoAccessTokenRepository implements AccessTokenRepository {
    @Autowired
    private MongoDb mongoDb;

    public Optional<AccessToken> findByToken(final String token) {
        final MongoCollection<AccessToken> coll = mongoDb.getMongoDatabase().getCollection(AccessToken.COLLECTION_NAME, AccessToken.class);
        final Bson query = new Document("token", token);
        return Optional.ofNullable(coll.find(query).limit(1).first());
    }

    public boolean upsert(final AccessToken accessToken) {
        return mongoDb.getMongoDatabase().getCollection(AccessToken.COLLECTION_NAME, AccessToken.class)
                .updateOne(Filters.eq("accountId", accessToken.getAccountId()),
                        new Document().append("$set", new Document().append("accountId", accessToken.getAccountId()).append("token", accessToken.getToken()).append("stamp", accessToken.getStamp())),
                        new UpdateOptions().upsert(true)).wasAcknowledged();
    }

    @Override
    public Optional<Boolean> removeByToken(String token) {
        return Optional.of(mongoDb.getMongoDatabase().getCollection(AccessToken.COLLECTION_NAME, AccessToken.class)
                .deleteOne(new Document("token", token)).getDeletedCount() == 1);
    }
}
