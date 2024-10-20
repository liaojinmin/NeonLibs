import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":project:module-nms"))
    compileOnly(project(":project:module-nms-data-serializer"))
    // 服务端
    compileOnly("ink.ptms.core:v12101:12101-minimize:mapped")
    // DataSerializer
    compileOnly("io.netty:netty-all:4.1.73.Final")
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}