package com.github.llyb120.namilite.config;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.github.llyb120.namilite.error.ControllerException;
import com.github.llyb120.namilite.func.Arg1Function;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import static com.github.llyb120.json.Json.o;

public class NamiConfig {

    public ControllerException controllerException(Object ...args){
        String msg = "";
        if(args.length > 0 && args[0] instanceof String){
            msg = (String) args[0];
            Object[] _args = Arrays.copyOfRange(args, 1, args.length);
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

    public String[] hotPackages(){
        return new String[0];
    }


    public final Set<String> getFullHotPackages(){
        Set<String> set = new ConcurrentHashSet<>(Arrays.asList(hotPackages()));
        set.addAll(controlPackages().values());
        return set;
    }


}
