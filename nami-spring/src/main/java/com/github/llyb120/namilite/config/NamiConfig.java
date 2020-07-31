package com.github.llyb120.namilite.config;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.github.llyb120.namilite.api.EasyApi;
import com.github.llyb120.namilite.api.EasyRule;
import com.github.llyb120.namilite.error.ControllerException;
import com.github.llyb120.namilite.func.Arg1Function;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import static com.github.llyb120.json.Json.a;
import static com.github.llyb120.json.Json.o;

public class NamiConfig {

    public ControllerException controllerException(Object ...args){
        String msg = "";
        if(args.length > 0 && args[0] instanceof String){
            msg = (String) args[0];
            Object[] _args;
            if(args.length > 1 && args[1] instanceof Object[]){
                _args = (Object[])args[1];//Arrays.copyOfRange(args, 1, args.length);
            } else {
                _args = Arrays.copyOfRange(args, 1, args.length);
            }
            return new ControllerException(msg, _args);
        } else {
            return new ControllerException();
        }
    }

    public NamiAuth namiAuth(){
        return new NamiAuth();
    }

    public Object resultOk(Object data){
        return R.ok(data);
    }

    public Object resultError(ResultErrorType type, Object arg){
        if(type == ResultErrorType.CONTROLLER_EXCEPTION){
            return R.error((String) arg);
        } else if(type == ResultErrorType.UN_LOGIN){
            return R.error("没有登陆");
        } else {
            return R.error(String.valueOf(arg));
        }
    }

//    public String controllerPackage(){
//        return "";
//    }

    public Map controlPackages(){
        return o();
    }

    public List<String> hotPackages(){
        return a();//new String[0];
    }

    public List<String> springHotPackages(){
        return a();
    }

    public void easyApi(){
        EasyApi.registerMongoDB("db", null)
                .bind("/api/mongo")
                .rule(new EasyRule()
                        .collection("fu")
                        .fields("fuck", "you", "oh"));
//        return o();
    }

    public final Set<String> getFullHotPackages(){
        Set<String> set = new ConcurrentHashSet(hotPackages());
        set.addAll(controlPackages().values());
        return set;
    }


}
