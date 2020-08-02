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
        boolean maybe = false;
        for (String hotPackage : namiConfig.springHotPackages()) {
            if(hotPackage.startsWith("!")){
                hotPackage = hotPackage.substring(1);
                if (clzName.startsWith(hotPackage)) {
                    maybe = false;
                    break;
                }
            } else if(clzName.startsWith(hotPackage)) {
                maybe = true;
            }
        }
        return maybe;
    }
}
