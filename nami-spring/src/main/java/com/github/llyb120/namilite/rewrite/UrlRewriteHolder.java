package com.github.llyb120.namilite.rewrite;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UrlRewriteHolder {
    public static ThreadLocal<String> localPath = new ThreadLocal<>();
    public void run(HttpServletRequest request, HttpServletResponse response){
        localPath.set(request.getServletPath());
    }
}
