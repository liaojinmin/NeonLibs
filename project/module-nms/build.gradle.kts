dependencies {
    compileOnly(project(":common-env"))
    compileOnly(project(":common"))

    // 服务端
    compileOnly("ink.ptms.core:v12101:12101-minimize:mapped")
    compileOnly("ink.ptms.core:v11604:11604")
    // DataSerializer
    compileOnly("io.netty:netty-all:5.0.0.Alpha2")


}