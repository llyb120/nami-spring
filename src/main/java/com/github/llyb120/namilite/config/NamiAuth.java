package com.github.llyb120.namilite.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class NamiAuth {

    public boolean checkLogin(HttpServletRequest request,
                              HttpServletResponse response){
        return true;
    }

    public boolean isSu(HttpServletRequest request,
                        HttpServletResponse response){
        return true;
    }



}
