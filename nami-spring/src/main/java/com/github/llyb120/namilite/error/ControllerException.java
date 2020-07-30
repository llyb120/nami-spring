package com.github.llyb120.namilite.error;

public class ControllerException extends NamiBaseException {

    public ControllerException() {
        super();
    }

    public ControllerException(String msg, Object... objects) {
        super(msg, objects);
    }

}
