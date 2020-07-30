package com.github.llyb120.namilite.config;

public final class R <T> {
    public boolean success;
    public T data;
    public String errMessage;

    protected R(){}

    public static <T> R ok(T data){
        R r = new R();
        r.success = true;
        r.data = data;
        return r;
    }

    public static <T> R error(String errMessage){
        R r = new R();
        r.success = false;
        r.errMessage = errMessage;
        return r;
    }


}
