import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":project:module-nms"))
    compileOnly(project(":project:module-common"))
    // 服务端
   // compileOnly("net.md-5:bungeecord-chat:1.17")
    compileOnly("ink.ptms:nms-all:1.0.0")
    compileOnly("ink.ptms.core:v12002:12002-minimize:mapped")
    compileOnly("ink.ptms.core:v12002:12002-minimize:universal")
    compileOnly("ink.ptms.core:v12004:12004-minimize:universal")
    // Mojang
    //compileOnly("com.mojang:brigadier:1.0.18")
    // DataSerializer
    compileOnly("io.netty:netty-all:5.0.0.Alpha2")

    compileOnly("org.tabooproject.reflex:reflex:1.1.6")
    compileOnly("org.tabooproject.reflex:analyser:1.1.6")
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
