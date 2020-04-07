package com.plugin.asm

import org.objectweb.asm.Opcodes

class CoustomHookConfig {
    /**
     * android.gradle 3.2.1 版本中，针对 Lambda 表达式处理
     */
    public final static HashMap<String, CoustomMethodCell> LAMBDA_METHODS = new HashMap<>()
    static {
        CoustomMethodCell onclick = new CoustomMethodCell(
                'onClick',
                '(Landroid/view/View;)V',
                'Landroid/view/View$OnClickListener;',
                'logDetail',
                '(Landroid/view/View;)V',
                1, 1,
                [Opcodes.ALOAD]
        )
        LAMBDA_METHODS.put(onclick.parent + onclick.name + onclick.desc, onclick)
    }
}