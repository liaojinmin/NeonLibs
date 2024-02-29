
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    `maven-publish`
    java
    id("org.jetbrains.kotlin.jvm") version "1.7.20"
    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
}

subprojects {
    apply<JavaPlugin>()
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "maven-publish")
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://maven.aliyun.com/repository/central")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://repo.tabooproject.org/repository/releases")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://repo.codemc.org/repository/maven-public")
        maven("https://repo.rosewooddev.io/repository/public/")
        maven("https://repo.opencollab.dev/maven-snapshots/")
        maven("https://jitpack.io")
        maven {
            url = uri("http://ptms.ink:8081/repository/releases/")
            isAllowInsecureProtocol = true
        }
    }


    dependencies {
        compileOnly(kotlin("stdlib"))

        compileOnly("com.google.code.gson:gson:2.8.5")
        compileOnly("com.google.guava:guava:21.0")

        compileOnly("redis.clients:jedis:4.2.2")

        compileOnly("com.zaxxer:HikariCP:4.0.3")

        // server
        compileOnly("ink.ptms.core:v11604:11604")

    }
    java {
        withSourcesJar()
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xjvm-default=all", "-Xextended-compiler-checks")
        }
    }
}


gradle.buildFinished {
    buildDir.deleteRecursively()
}


