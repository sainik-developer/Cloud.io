package com.cloudio.backend.mongo;

import com.cloudio.backend.repository.AccountRepository;
import com.cloudio.backend.model.AccountStatus;
import com.cloudio.backend.model.AccountType;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.cloudio.backend.model.Account;
import lombok.extern.log4j.Log4j2;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Log4j2
@Repository
public  class MongoAccountRepository implements AccountRepository {

    @Autowired
    private MongoDb mongoDb;

    public MongoAccountRepository() {

    }

    public Optional<List<Account>> findByPhoneNumber(final String phoneNumber) {
        final MongoCollection<Account> coll = mongoDb.getMongoDatabase().getCollection(Account.COLLECTION_NAME, Account.class);
        final Bson query = new Document("phoneNumber", phoneNumber).append("status", AccountStatus.ACTIVE.toString());
        return Optional.ofNullable(coll.find(query).into(new LinkedList<>()));
    }

    public Optional<Account> findByPhoneNumberAndCompanyId(final String phoneNumber, final String companyId) {
        final MongoCollection<Account> coll = mongoDb.getMongoDatabase().getCollection(Account.COLLECTION_NAME, Account.class);
        final Bson query = new Document("phoneNumber", phoneNumber).append("companyId", companyId).append("status", AccountStatus.ACTIVE.toString()).append("status", AccountStatus.INVITED.toString());
        return Optional.ofNullable(coll.find(query).first());
    }

    public boolean upsert(final Account account) {
        return mongoDb.getMongoDatabase().getCollection(Account.COLLECTION_NAME, Account.class)
                .updateOne(Filters.eq("phoneNumber", account.getPhoneNumber()),
                        new Document().append("$set", new Document()
                                .append("accountId", account.getAccountId())
                                .append("companyId", account.getCompanyId())
                                .append("phoneNumber", account.getPhoneNumber())
                                .append("name", account.getName())
                                .append("updated", account.getUpdated())
                                .append("status", account.getStatus().toString())
                                .append("type", account.getType()==null? AccountType.MEMBER.toString() : account.getType().toString())),
                        new UpdateOptions().upsert(true)).wasAcknowledged();
    }

}
