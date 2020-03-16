package com.github.llyb120.namilite.api;

import com.mongodb.client.MongoDatabase;

public final class EasyApi {

    public static MongoApi registerMongoDB(String name, MongoDatabase db){
        return new MongoApi();
    }


}
