package com.github.llyb120.namilite;


import cn.hutool.core.io.FileUtil;
import com.github.llyb120.json.Obj;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static com.github.llyb120.json.Json.o;
import static com.github.llyb120.namilite.init.NamiBean.namiConfig;


public abstract class NamiBaseController {
    protected Obj $get = o();
    protected Obj $post = o();
    protected Obj $request = o();
    protected Map<String, MultipartFile> $files = new HashMap<>();

    public void onRequest(HttpServletRequest request, Map body, Map files){
        Enumeration<String> elems = request.getParameterNames();
        while (elems.hasMoreElements()){
            String key = elems.nextElement();
            $get.put(key, request.getParameter(key));
        }
        if (body != null) {
            $post.putAll(body);
        }
        if(files != null){
            $files.putAll(files);
        }
        $request.putAll($get);
        $request.putAll($post);
    }



    protected HttpEntity download(byte[] bs, String fileName) {
        return download(bs, fileName, MediaType.APPLICATION_OCTET_STREAM_VALUE);
    }

    protected HttpEntity download(byte[] bs, String fileName, String contentType) {
        return new HttpEntity(bs, genHeaders(fileName, contentType));
    }

    protected HttpEntity download(File file, String fileName, boolean delTemp){
        byte[] bs = FileUtil.readBytes(file);
        if(delTemp){
            file.delete();
        }
        return download(bs, fileName, MediaType.APPLICATION_OCTET_STREAM_VALUE);
    }

    protected HttpEntity download(File file, String fileName){
        return download(file, fileName, MediaType.APPLICATION_OCTET_STREAM_VALUE);
    }

    protected HttpEntity download(File file, String fileName, String contentType){
        return new HttpEntity(FileUtil.readBytes(file), genHeaders(fileName, contentType));
    }

    protected HttpHeaders genHeaders(String fileName, String contentType){
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, contentType);//MediaType.APPLICATION_OCTET_STREAM_VALUE);
        try {
            fileName = URLEncoder.encode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw namiConfig.controllerException("下载失败");
        }
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"; filename*=utf-8''" + fileName);
        return headers;
    }


    protected void error() {
        throw namiConfig.controllerException();
    }

    protected void error(String tmpl, Object ...args){
        throw namiConfig.controllerException(tmpl, args);
    }
}
