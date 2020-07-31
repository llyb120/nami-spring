package com.github.llyb120.namilite.init;

import com.github.llyb120.namilite.config.NamiAuth;
import com.github.llyb120.namilite.config.NamiConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class NamiBean {
    public static NamiConfig namiConfig;
    public static NamiAuth namiAuth;
    public static volatile boolean hasBeetlSql = false;

    @Autowired
    public void set(
            NamiConfig config
    ){
        namiConfig = config;
        namiAuth = config.namiAuth();

        //init beetl
        try{
            Class.forName("org.beetl.sql.core.DSTransactionManager");
            hasBeetlSql = true;
        } catch (Exception e){
            hasBeetlSql = false;
        }


    }


}
