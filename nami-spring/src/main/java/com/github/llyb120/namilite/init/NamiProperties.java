package com.github.llyb120.namilite.init;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Administrator
 * @Date: 2020/9/20 18:43
 */
@ConfigurationProperties(prefix = "nami")
public class NamiProperties {

    private List<String> springHotPackages = new ArrayList<>();
    private int compileWaitSeconds = 5;

    public List<String> getSpringHotPackages() {
        return springHotPackages;
    }

    public void setSpringHotPackages(List<String> springHotPackages) {
        this.springHotPackages = springHotPackages;
    }

    public int getCompileWaitSeconds() {
        if(compileWaitSeconds <= 0){
            compileWaitSeconds = 5;
        }
        return compileWaitSeconds;
    }

    public void setCompileWaitSeconds(int compileWaitSeconds) {
        this.compileWaitSeconds = compileWaitSeconds;
    }
}
