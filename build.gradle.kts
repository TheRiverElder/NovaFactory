import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    application
    kotlin("jvm") version "1.5.21"
}

group = "io.theriverelder.novafactory"
version = "0.0.1"
application {
    mainClass.set("io.theriverelder.novafactory.ApplicationKt")
}

repositories {
    maven(url = "https://maven.aliyun.com/repository/central")
    maven(url = "https://maven.aliyun.com/repository/public")
    maven(url = "https://maven.aliyun.com/repository/gradle-plugin")
    maven(url = "https://maven.aliyun.com/repository/apache-snapshots")
    mavenLocal()
    mavenCentral()
    google()
    jcenter()
}

//buildScript {
//    repositories {
//        maven(url = "https://maven.aliyun.com/repository/central")
//        maven(url = "https://maven.aliyun.com/repository/public")
//        maven(url = "https://maven.aliyun.com/repository/gradle-plugin")
//        maven(url = "https://maven.aliyun.com/repository/apache-snapshots")
//        mavenLocal()
//        mavenCentral()
//        google()
//        jcenter()
//    }
//}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-websockets:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlin_version")
    implementation(kotlin("stdlib-jdk8"))
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}