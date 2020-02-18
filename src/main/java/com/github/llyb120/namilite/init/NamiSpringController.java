package com.github.llyb120.namilite.init;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ClassUtil;

import com.github.llyb120.json.Json;
import com.github.llyb120.namilite.NamiBaseController;
import com.github.llyb120.namilite.config.ResultErrorType;
import com.github.llyb120.namilite.init.NamiLite;
import com.github.llyb120.namilite.boost.ErrorMessage;
import com.github.llyb120.namilite.boost.OnlySu;
import com.github.llyb120.namilite.boost.UnLogin;
import com.github.llyb120.namilite.rewrite.UrlRewriteHolder;
import org.beetl.sql.core.DSTransactionManager;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Map;

import static com.github.llyb120.json.Json.o;
import static com.github.llyb120.namilite.init.NamiBean.*;

@RequestMapping("/nami")
@RestController
public class NamiSpringController {

    @RequestMapping("/{clz}/{method}")
    public Object call(
        @PathVariable String clz,
        @PathVariable String method,
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestParam(required = false) Map params,
        @RequestPart(required = false) MultipartFile file
        ) throws Exception{
        String contentType = request.getContentType();
        if(contentType != null && contentType.contains("application/json")){
            String str = IoUtil.read(request.getReader());
            Object object = Json.parse(str);
            if(object instanceof Map){
                params.putAll((Map) object);
            }
        }
        if(method.equals("undefined")){
            return page404();
        }
        String className = null;
        String url = UrlRewriteHolder.localPath.get();
        for (NamiSpringFilter.Route route : NamiSpringFilter.routes) {
            if(route.matches(url)){
                className = route.packageName + "." + clz; //"com.beeasy.v20.hot.ctrl." + clz;
                break;
            }
        }
        if (className == null) {
            return page404();
        }
        Class clzz = NamiLite.Clz(className);
        Object instance = clzz.newInstance();//V20.Bean(className);
//        Class clzz = instance.getClass();
        //php注入
        if(!NamiBaseController.class.isAssignableFrom(clzz)){
            return page404();
        }
        ((NamiBaseController)instance).onRequest(request, params, o("file", file));

        boolean checkLogin = true;
        UnLogin unLogin = (UnLogin) clzz.getAnnotation(UnLogin.class);
        if (unLogin != null) {
            checkLogin = false;
        }
        while(clzz != null && !clzz.getName().equals(NamiBaseController.class.getName())){
            for (Method declaredMethod : clzz.getDeclaredMethods()) {
                if(!ClassUtil.isPublic(declaredMethod)){
                    continue;
                }
                if(declaredMethod.getName().equals(method)){
                    if(checkLogin){
                        unLogin = declaredMethod.getAnnotation(UnLogin.class);
                        if (unLogin == null) {
                            //校验登录
                            if(!namiAuth.checkLogin(request, response)){
                                return namiConfig.resultError(ResultErrorType.UN_LOGIN, null);
                            }
                        }
                    }
                    //检查SU
                    OnlySu onlySu = declaredMethod.getAnnotation(OnlySu.class);
                    if(onlySu != null && !namiAuth.isSu(request,response)){
                        return page404();
                    }
                    //拉出结果
                    Object ret = null;
                    try{
                        if(hasBeetlSql){
                            DSTransactionManager.start();
                        }
                        ret = declaredMethod.invoke(instance);
                        if(hasBeetlSql){
                            DSTransactionManager.commit();
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                        try {
                            if(hasBeetlSql){
                                DSTransactionManager.rollback();
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                        ErrorMessage errorMessage = declaredMethod.getAnnotation(ErrorMessage.class);
                        if(errorMessage != null){
                            return namiConfig.resultError(ResultErrorType.CONTROLLER_EXCEPTION, errorMessage.value());
                        }
                        return namiConfig.resultError(ResultErrorType.CONTROLLER_EXCEPTION, e.getMessage());
                    }
                    RequestMapping mapping = declaredMethod.getAnnotation(RequestMapping.class);
                    if (mapping == null) {
                        if(ret instanceof HttpEntity){
                            return ret;
                        }
                        return namiConfig.resultOk(ret);//Result.ok(ret);
                    }
                    String[] header = mapping.produces();
                    if(header.length > 0){
                        HttpHeaders headers = new HttpHeaders();
                        headers.add(HttpHeaders.CONTENT_TYPE, String.join("; ", header));
                        return new ResponseEntity<>(ret, headers, HttpStatus.OK);
                    } else {
                        return namiConfig.resultOk(ret);//Result.ok(ret);
                    }
                }
            }

            clzz = clzz.getSuperclass();
        }

        //404
        return page404();
    }

    public HttpEntity page404(){
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE);
        return new ResponseEntity<>("", headers, HttpStatus.NOT_FOUND);
    }

}
