package com.plugin.test;

import org.objectweb.asm.util.ASMifier;

/**
 * <p />Author: chenming
 * <p />E-mail: cm1@erongdu.com
 * <p />Date: 2020-04-03 17:53
 * <p />Description:
 */
public class Test1 {

    public static void main(String[] args) {
        // 解析java编译后的class类为ASM编码格式文件输出
        System.out.println("12345678");
        try {
            ASMifier.main(new String[]{"com.plugin.test.BaseActivity"});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
