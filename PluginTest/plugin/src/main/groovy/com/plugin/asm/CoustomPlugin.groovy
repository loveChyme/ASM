package com.plugin.asm


import com.android.build.gradle.AppExtension
import com.sun.istack.NotNull
import org.gradle.api.Plugin
import org.gradle.api.Project

class CoustomPlugin implements Plugin<Project> {
    @Override
    void apply(@NotNull Project project) {
        System.out.println("CoustomPlugin-----------------------")
        println"--------------------------apply-----------------------------"
        AppExtension extension = project.extensions.findByType(AppExtension.class)
        extension.registerTransform(new CoustomTransform())
        println"--------------------------apply end-----------------------------"
    }
}