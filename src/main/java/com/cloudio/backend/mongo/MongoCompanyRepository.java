package com.cloudio.backend.mongo;

import com.cloudio.backend.repository.CompanyRepository;
import com.cloudio.backend.model.Company;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public class MongoCompanyRepository implements CompanyRepository {

    private MongoDatabase mongoDatabase;

    @Autowired
    private MongoDb mongoDb;
    public MongoCompanyRepository() {
    }

    @Override
    public Optional<Company> findByCompanyId(String companyId) {
        final MongoCollection<Company> coll = mongoDb.getMongoDatabase().getCollection(Company.COLLECTION_NAME, Company.class);
        final Bson query = new Document("companyId", companyId);
        return Optional.ofNullable(coll.find(query).first());
    }
    @Override
    public Optional<Company> saveCompany(Company company) {
        final MongoCollection<Company> coll = mongoDb.getMongoDatabase().getCollection(Company.COLLECTION_NAME, Company.class);
        coll.insertOne(company);
        return findByCompanyId(company.getCompanyId());
    }



}
