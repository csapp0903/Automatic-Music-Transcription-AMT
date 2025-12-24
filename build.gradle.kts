// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        // 阿里云镜像 - 优先使用
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }

        // 备用官方源
        google()
        mavenCentral()
    }
}

plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
}

//allprojects {
//    repositories {
//        // 阿里云镜像 - 优先使用
//        maven { url = uri("https://maven.aliyun.com/repository/google") }
//        maven { url = uri("https://maven.aliyun.com/repository/public") }
//        maven { url = uri("https://maven.aliyun.com/repository/central") }
//
//        // 备用官方源
//        google()
//        mavenCentral()
//    }
//}
