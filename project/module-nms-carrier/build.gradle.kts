import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-env"))
    compileOnly(project(":project:module-nms"))
    compileOnly(project(":project:module-nms-data-serializer"))
    compileOnly(project(":project:module-common"))

    // 服务端
   // compileOnly("net.md-5:bungeecord-chat:1.17")
    compileOnly("ink.ptms.core:v11604:11604")
    compileOnly("ink.ptms.core:v11900:11900:mapped")
    compileOnly("ink.ptms.core:v11900:11900:universal")
    compileOnly("ink.ptms.core:v11903:11903:mapped")
    compileOnly("ink.ptms.core:v11800:11800:mapped")
    compileOnly("ink.ptms:nms-all:1.0.0")
    // Mojang
    //compileOnly("com.mojang:brigadier:1.0.18")
    // DataSerializer
    compileOnly("io.netty:netty-all:5.0.0.Alpha2")
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
     //   relocate("org.tabooproject", "taboolib.library")
    }
    build {
        dependsOn(shadowJar)
    }
}
