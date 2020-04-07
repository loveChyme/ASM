package com.plugin.asm

import org.objectweb.asm.*

class CoustomClassVisitor extends ClassVisitor implements Opcodes {
    private final static String SDK_API_CLASS = "com/plugin/logsdk/TestLog"
    private ClassVisitor classVisitor
    private String[] mInterfaces
    private HashMap<String, CoustomMethodCell> mLambdaMethodCells = new HashMap<>()

    CoustomClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM6, cv)
        this.classVisitor = cv
    }

    private static void visitMethodWithLoadedParams(MethodVisitor methodVisitor, int opcode, String owner, String methodName, String methodDesc, int start,
                                                    int count, List<Integer> paramOpcodes) {
        for (int i = start; i < start + count; i++) {
            methodVisitor.visitVarInsn(paramOpcodes[i - start], i)
        }
        methodVisitor.visitMethodInsn(opcode, owner, methodName, methodDesc, false)
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces)
        this.mInterfaces = interfaces
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions)

        String nameDesc = name + desc

        methodVisitor = new CoustomDefaultMethodVisitor(methodVisitor, access, name, desc) {
            @Override
            void visitEnd() {
                super.visitEnd()

                if (mLambdaMethodCells.containsKey(nameDesc)) {
                    mLambdaMethodCells.remove(nameDesc)
                }
            }
            @Override
            void visitInvokeDynamicInsn(String name1, String desc1, Handle bsm, Object... bsmArgs) {
                super.visitInvokeDynamicInsn(name1, desc1, bsm, bsmArgs)
                //
                try {
                    String desc2 = (String) bsmArgs[0]
                    // 获取方法名称
                    CoustomMethodCell cell = CoustomHookConfig.LAMBDA_METHODS.get(Type.getReturnType(desc1).getDescriptor() + name1 + desc2)
                    if (cell != null) {
                        Handle it = (Handle) bsmArgs[1]
                        mLambdaMethodCells.put(it.name + it.desc, cell)
                    }
                } catch (Exception e) {
                    e.printStackTrace()
                }
            }

            @Override
            protected void onMethodEnter() {
                super.onMethodEnter()

                println "--------------------onMethodEnter----------------------------"

                CoustomMethodCell lambdaCell = mLambdaMethodCells.get(nameDesc)
                if (lambdaCell != null) {
                    println "access" + access

                    println "name" + name
                    println "desc" + desc
                    println "signature" + signature
                    Type[] types = Type.getArgumentTypes(lambdaCell.desc)
                    int length = types.length
                    Type[] lambdaTypes = Type.getArgumentTypes(desc)
                    int paramStart = lambdaTypes.length - length
                    if (paramStart < 0)
                        return
                    else {
                        for (int i = 0; i < length; i++) {
                            if (lambdaTypes[paramStart + i].descriptor != types[i].descriptor)
                                return
                        }
                    }

                    boolean isStaticMethod = CoustomUtils.isStatic(access)
                    if (!isStaticMethod) {
                        // 静态方法中，判断方法是否为匹配类型，如果匹配则直接注入相对应代码
                        if (lambdaMethodCell.desc == '(Landroid/view/MenuItem;)Z') {
                            methodVisitor.visitVarInsn(ALOAD, 0)
                            methodVisitor.visitVarInsn(ALOAD, getVisitPosition(lambdaTypes, paramStart, isStaticMethod))
                            methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS, lambdaMethodCell.agentName, '(Ljava/lang/Object;' +
                                    'Landroid/view/MenuItem;)V', false)
                            return
                        }
                    }
                    for (int i = 0; i < paramStart + lambdaCell.paramsCount; i++) {
                        println "(i - paramsStart)" + (i - paramStart) + " ***** getVisitPosition(lambdaTypes, i, isStaticMethod)" + getVisitPosition(lambdaTypes, i, isStaticMethod)
                        println "i==" + i + "****paramStart===" + paramStart + "****lambdaTypes===" + lambdaTypes + "****i===" + i + "****isStaticMethod===" + isStaticMethod
                        methodVisitor.visitVarInsn(lambdaCell.opcodes.get(i - paramStart), getVisitPosition(lambdaTypes, i, isStaticMethod))
                    }
                    methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS, lambdaCell.agentName, lambdaCell.agentDesc, false)
                    println "--------------------onMethodEnter lambda end----------------------------"
                    return
                }

                if (mInterfaces != null && mInterfaces.length > 0) {
                    println "--------------------onMethodEnter common edit----------------------------"
                    if (mInterfaces.contains('android/view/View$OnClickListener') && nameDesc == 'onClick(Landroid/view/View;)V') {
                        methodVisitor.visitVarInsn(ALOAD, 1)
                        methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS, "logDetail", "(Landroid/view/View;)V", false)
                    }
                }
                println "--------------------onMethodEnter common end----------------------------"
            }

            @Override
            AnnotationVisitor visitAnnotation(String s, boolean b) {
                /*if (s == 'Lcom/sensorsdata/analytics/android/sdk/SensorsDataTrackViewOnClick;') {
                    isSensorsDataTrackViewOnClickAnnotation = true
                }*/

                return super.visitAnnotation(s, b)
            }
        }

        return methodVisitor
    }

    /**
     * 获取方法参数下标为 index 的对应 ASM index
     * @param types 方法参数类型数组
     * @param index 方法中参数下标，从 0 开始
     * @param isStaticMethod 该方法是否为静态方法
     * @return 访问该方法的 index 位参数的 ASM index
     */
    int getVisitPosition(Type[] types, int index, boolean isStaticMethod) {
        if (types == null || index < 0 || index >= types.length) {
            throw new Error("getVisitPosition error")
        }
        if (index == 0) {
            return isStaticMethod ? 0 : 1
        } else {
            return getVisitPosition(types, index - 1, isStaticMethod) + types[index - 1].getSize()
        }
    }

    /**
     * 动态生成class
     * @return
     */
    public byte[] createNewClass() {
        //创建ClassWriter ，构造参数的含义是是否自动计算栈帧，操作数栈及局部变量表的大小
        //0：完全手动计算 即手动调用visitFrame和visitMaxs完全生效
        //ClassWriter.COMPUTE_MAXS=1：需要自己计算栈帧大小，但本地变量与操作数已自动计算好，当然也可以调用visitMaxs方法，只不过不起作用，参数会被忽略；
        //ClassWriter.COMPUTE_FRAMES=2：栈帧本地变量和操作数栈都自动计算，不需要调用visitFrame和visitMaxs方法，即使调用也会被忽略。
        //这些选项非常方便，但会有一定的开销，使用COMPUTE_MAXS会慢10%，使用COMPUTE_FRAMES会慢2倍。
        ClassWriter cw = new ClassWriter(0);
        // 初始化init 构造方法，默认的asm不会自动生成构造函数
        mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        // aload_0
        mv.visitVarInsn(ALOAD, 0);
        // 获取变量的值，
        mv.visitMethodInsn(INVOKESPECIAL,"java/lang/Object", "<init>", "()V", false);
        // 初始化构造函数变量
        mv.visitVarInsn(ALOAD, 0);
        // 给构造函数变量赋值
        mv.visitIntInsn(BIPUSH, 10);
        mv.visitFieldInsn(PUTFIELD, "asm/Student", "age", "I");
        // 结束
        mv.visitInsn(RETURN);
        // 设置操作数栈和本地变量表的大小
        mv.visitMaxs(2, 1);
        //结束方法生成
        mv.visitEnd();
        //  创建get方法
        //创建类头部信息：jdk版本，修饰符，类全名，签名信息，父类，接口集
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "asm/Student", null, "java/lang/Object", null);
        //创建字段age：修饰符，变量名，类型，签名信息，初始值（不一定会起作用后面会说明）
        cw.visitField(Opcodes.ACC_PUBLIC , "age", "I", null, new Integer(11))
                .visitEnd();
        //创建方法：修饰符，方法名，类型，描述（输入输出类型），签名信息，抛出异常集合
        // 方法的逻辑全部使用jvm指令来书写的比较晦涩，门槛较高，后面会介绍简单的方法
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getAge", "()I", null, null);
        // 创建方法第一步
        mv.visitCode();
        // 将索引为 #0 的本地变量列表加到操作数栈下。#0 索引的本地变量列表永远是 this ，当前类实例的引用。
        mv.visitVarInsn(ALOAD, 0);
        // 获取变量的值，
        mv.visitFieldInsn(GETFIELD, "asm/Student", "age", "I");
        // 返回age
        mv.visitInsn(IRETURN);
        // 设置操作数栈和本地变量表的大小
        mv.visitMaxs(1, 1);
        //结束方法生成
        mv.visitEnd();
        //结束类生成
        cw.visitEnd();
        //返回class的byte[]数组
        return cw.toByteArray();
    }
}