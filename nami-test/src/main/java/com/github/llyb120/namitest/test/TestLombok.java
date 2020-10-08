package com.github.llyb120.namitest.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: Administrator
 * @Date: 2020/8/1 16:56
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestLombok {
    private int a = 1;
    private int b = 2;
    private String ccc;
    private String d;
    private String foo = "a!!!ffck";
}
