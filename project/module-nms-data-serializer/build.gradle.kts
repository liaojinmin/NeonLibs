
dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":project:module-nms"))
    // 服务端
    compileOnly("ink.ptms.core:v12101:12101-minimize:mapped")
    // DataSerializer
    compileOnly("io.netty:netty-all:4.1.73.Final")
}
