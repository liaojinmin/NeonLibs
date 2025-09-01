dependencies {
    compileOnly(project(":common-env"))
    compileOnly(project(":common"))

    // 服务端
    compileOnly("ink.ptms.core:v12101:12101-minimize:mapped")
    compileOnly("ink.ptms.core:v11604:11604")
    // DataSerializer
    compileOnly("io.netty:netty-all:5.0.0.Alpha2")

    compileOnly("de.tr7zw:item-nbt-api:2.15.2")
    compileOnly("org.tabooproject.reflex:reflex:1.1.6")
    compileOnly("org.tabooproject.reflex:analyser:1.1.6")


}