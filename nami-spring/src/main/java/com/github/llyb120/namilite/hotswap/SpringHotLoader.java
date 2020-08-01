package com.github.llyb120.namilite.hotswap;

import static com.github.llyb120.namilite.init.NamiBean.namiConfig;

/**
 * @Author: Administrator
 * @Date: 2020/7/31 17:42
 */
public class SpringHotLoader extends NamiHotLoader{
    @Override
    public boolean isHotClass(String clzName) {
        if(super.isHotClass(clzName)){
            return true;
        }
        for (String hotPackage : namiConfig.springHotPackages()) {
            if(clzName.startsWith(hotPackage)) {
                return true;
            }
        }
        return false;
    }
}
