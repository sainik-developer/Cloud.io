package com.cloudio.backend.mongo;

import com.cloudio.backend.utils.Properties;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import lombok.extern.log4j.Log4j2;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public final class MongoDb {

    private  MongoClient mongoClient;
    private  String mongoClusterURI;
    private  String mongoDbName;

    private  MongoDatabase mongoDatabase;
    @Autowired
    Properties properties;

    public MongoDb() {
    }


    public MongoDatabase getMongoDatabase() {
        if(mongoDatabase == null) {
            synchronized (this){
                if(mongoDatabase == null){
                    mongoClusterURI = properties.getMongo_cluster_uri();
                    mongoDbName = properties.getMongo_db_name();
                    log.info("mongo url = " + mongoClusterURI);
                    mongoClient = new MongoClient(new MongoClientURI(mongoClusterURI, MongoClientOptions.builder()));
                    mongoDatabase = mongoClient.getDatabase(mongoDbName).withCodecRegistry(CodecRegistries
                            .fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                                    CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())));

                }
            }
        }


        return mongoDatabase;
    }
}
