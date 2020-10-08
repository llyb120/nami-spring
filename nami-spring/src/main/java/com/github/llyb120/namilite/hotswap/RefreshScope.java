package com.github.llyb120.namilite.hotswap;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.llyb120.namilite.init.NamiLite.isDev;

/**
 * @Author: Administrator
 * @Date: 2020/10/8 16:54
 */
public class RefreshScope implements Scope {


    private static final String TARGET = "scopedTarget.";
    private static Map<String,Object> cache = new ConcurrentHashMap<>();

    //todo: 是否可以尝试用这个来接管热加载
    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        synchronized (name.intern()){
            Object item = cache.get(name);
            if (item == null) {
                item = objectFactory.getObject();
                cache.put(name, item);
            }
            return item;
        }
    }

    @Override
    public Object remove(String name) {
        return null;
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {

    }

    @Override
    public Object resolveContextualObject(String key) {
        return null;
    }

    @Override
    public String getConversationId() {
        return null;
    }


    public static void refresh(String name) {
        if(!isDev){
            return;
        }
        cache.remove(TARGET + name);
    }
    public static void refresh(){
        if(!isDev){
            return;
        }
        cache.clear();
    }
}
