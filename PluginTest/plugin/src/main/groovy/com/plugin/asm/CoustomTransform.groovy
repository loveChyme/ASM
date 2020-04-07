package com.plugin.asm

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import com.sun.istack.NotNull
import groovy.io.FileType
import org.apache.commons.codec.digest.DigestUtils

class CoustomTransform extends Transform {
    @Override
    String getName() {
        return "CoustomTransform"
    }

    /**
     * 处理文件类型
     * 返回class 标识处理java 的class文件 RESOURCES 代表需要处理java资源
     * @return
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * 处理文件作用域
     *      * 1. EXTERNAL_LIBRARIES        只有外部库
     *      * 2. PROJECT                   只有项目内容
     *      * 3. PROJECT_LOCAL_DEPS        只有项目的本地依赖(本地jar)
     *      * 4. PROVIDED_ONLY             只提供本地或远程依赖项
     *      * 5. SUB_PROJECTS              只有子项目。
     *      * 6. SUB_PROJECTS_LOCAL_DEPS   只有子项目的本地依赖项(本地jar)。
     *      * 7. TESTED_CODE               由当前变量(包括依赖项)测试的代码
     * @return
     */
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(@NotNull TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        _transform(transformInvocation.context, transformInvocation.inputs, transformInvocation.outputProvider, transformInvocation.incremental)
    }

    void _transform(Context context, Collection<TransformInput> inputs, TransformOutputProvider outputProvider, boolean isIncremental) {
        println '--------------- LifecyclePlugin visit start --------------- '
        /** Transform 的inputs两种类型，一种是目录，一种是jar包，需要分开遍历 */
        inputs.each {TransformInput input ->
            // 遍历目录
            input.directoryInputs.each {DirectoryInput directoryInput ->
                // 当前这个 transform 输出目录
                File dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                File dir = directoryInput.file
                //println "--------------------------_transform-----------------------------"
                if (dir) {
                    HashMap<String, File> modifyMap = new HashMap<>()
                    //println "--------------------------transform dir-----------------------------"
                    dir.traverse(type: FileType.FILES, nameFilter: ~/.*\.class/) {
                        File classFile ->
                            //println"--------------------------classFile.name-----------------------------"
                            //println classFile.name
                            if (AnalyticsClassModifier.isShouldModify(classFile.name)) {
                                println("shouldModify" + classFile.name)
                                File modified = null
                                modified = AnalyticsClassModifier.modifyClassFile(dir, classFile, context.getTemporaryDir())
                                if (modified != null) {
                                    // key 为包名 + 类名
                                    String key = classFile.absolutePath.replace(dir.absolutePath, "")
                                    println "key" + key
                                    modifyMap.put(key, modified)
                                }
                            }
                    }
                    //println( "directoryInput" + directoryInput.file.getAbsolutePath()+"******dest" + dest.absolutePath)
                    FileUtils.copyDirectory(directoryInput.file, dest)
                    modifyMap.entrySet().each {
                        Map.Entry<String, File> en ->
                            File target = new File(dest.absolutePath + en.getKey())
                            println ("*****key***" + en.getKey())
                            println ("*****value***" + en.getValue())

                            if (target.exists()) {
                                target.delete()
                            }
                            FileUtils.copyFile(en.getValue(), target)
                            en.getValue().delete()
                    }
                }
            }
            println( "------------jarInputs--------------")
            /**遍历 jar*/
            input.jarInputs.each { JarInput jarInput ->
                String destName = jarInput.file.name

                /**截取文件路径的 md5 值重命名输出文件,因为可能同名,会覆盖*/
                def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath).substring(0, 8)
                /** 获取 jar 名字*/
                if (destName.endsWith(".jar")) {
                    destName = destName.substring(0, destName.length() - 4)
                }

                /** 获得输出文件*/
                File dest = outputProvider.getContentLocation(destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                def modifiedJar = null;
                /*if (!sensorsAnalyticsExtension.disableAppClick) {
                    modifiedJar = SensorsAnalyticsClassModifier.modifyJar(jarInput.file, context.getTemporaryDir(), true)
                }*/
                if (modifiedJar == null) {
                    modifiedJar = jarInput.file
                }
                org.apache.commons.io.FileUtils.copyFile(modifiedJar, dest)
            }
        }
    }
}