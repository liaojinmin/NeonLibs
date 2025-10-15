
dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":project:module-nms"))
    // 服务端
    compileOnly("ink.ptms.core:v11604:11604")
    compileOnly("ink.ptms.core:v12101:12101-minimize:mapped")
    //compileOnly("ink.ptms.core:v12004:12004:mapped")
    //compileOnly("ink.ptms.core:v10900:10900")
    // DataSerializer
    compileOnly("io.netty:netty-all:4.1.73.Final")
}
