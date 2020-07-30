package com.github.llyb120.namilite.error;

public class NamiBaseException extends RuntimeException{
    public NamiBaseException(){
        super();
    }
    public NamiBaseException(String msg, Object... objects){
        super(String.format(msg,objects));
    }
}
