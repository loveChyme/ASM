package com.plugin.asm;

/**
 * <p />Author: chenming
 * <p />E-mail: cm1@erongdu.com
 * <p />Date: 2020-04-03 17:34
 * <p />Description:
 */
public class BaseActivity {
    public  int      a      = 10;
    private String[] arrays = new String[10];

    public void getStatic() {
        System.out.println("getStatic");
    }

    private void print() {
        System.out.println("a" + a);
        System.out.println("arrays" + arrays[0]);
    }
}
