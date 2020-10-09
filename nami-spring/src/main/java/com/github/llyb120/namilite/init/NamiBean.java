package com.github.llyb120.namilite.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class NamiBean {
    public static volatile boolean hasBeetlSql = false;
    public static NamiProperties namiProperties;

    @Autowired
    public void set(
            NamiProperties namiProperties
    ){
        NamiBean.namiProperties = namiProperties;
    }


}
