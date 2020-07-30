package com.github.llyb120.namilite.api;

import cn.hutool.core.collection.ConcurrentHashSet;
import org.apache.tomcat.util.digester.Rule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class MongoApi {

    public static Map<String, List<Rule>> bindings = new HashMap<>();
    private static ReentrantLock lock = new ReentrantLock();

    public MongoApi bind(String route){
//        lock.lock();
//        List<Rule> list = bindings.get(route);

//        bindings.add(route);
        return this;
    }

    public void rule(EasyRule rule){

    }

}
