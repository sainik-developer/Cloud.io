package com.cloudio.backend.mongo;

import com.amazonaws.util.StringUtils;
import com.cloudio.backend.repository.SignInCodeRepository;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.cloudio.backend.model.SignInDetails;
import lombok.extern.log4j.Log4j2;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Log4j2
@Repository

public  class MongoSignInCodeRepository implements SignInCodeRepository {

    @Autowired
    private MongoDb mongoDb;

    public MongoSignInCodeRepository() {

    }

    public Optional<SignInDetails> findByPhoneNumber(final String phoneNumber) {
        return Optional.ofNullable(mongoDb.getMongoDatabase().getCollection(SignInDetails.COLLECTION_NAME, SignInDetails.class)
                .find(new Document("phoneNumber", phoneNumber))
                .limit(1)
                .first());
    }

    public Optional<SignInDetails> upsert(final SignInDetails signInDetails) {
        if (signInDetails == null || StringUtils.isNullOrEmpty(signInDetails.getPhoneNumber()) || StringUtils.isNullOrEmpty(signInDetails.getSmsCode()))
            return Optional.empty();
        mongoDb.getMongoDatabase().getCollection(SignInDetails.COLLECTION_NAME, SignInDetails.class)
                .updateOne(Filters.eq("phoneNumber", signInDetails.getPhoneNumber()),
                        new Document().append("$set", new Document()
                                .append("smsCode", signInDetails.getSmsCode())
                                .append("phoneNumber", signInDetails.getPhoneNumber())
                                .append("updated", signInDetails.getUpdated())
                                .append("retry", signInDetails.getRetry())),
                        new UpdateOptions().upsert(true));
        return Optional.of(signInDetails);
    }
}
