import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    compileOnly(project(":project:module-nms"))
    compileOnly(project(":project:module-nms-data-serializer"))
    compileOnly(project(":common"))
    // 服务端
    compileOnly("ink.ptms.core:v12004:12004-minimize:mapped")
    compileOnly("ink.ptms.core:v11604:11604")
    // DataSerializer
    compileOnly("io.netty:netty-all:4.1.73.Final")
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}