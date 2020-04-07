package com.plugin.asm

import jdk.internal.org.objectweb.asm.util.ASMifier

class Test{
    static void main(String[] args) {
        ASMifier.main(["com.plugin.asm.BaseActivity"])
    }
}