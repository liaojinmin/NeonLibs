dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-env"))
    compileOnly(project(":project:module-nms"))
    // 服务端

    compileOnly("ink.ptms.core:v11904:11904-minimize:mapped")
    compileOnly("ink.ptms.core:v11604:11604")
    compileOnly("ink.ptms.core:v11600:11600-minimize")
    compileOnly("ink.ptms.core:v11200:11200-minimize")
    compileOnly("ink.ptms.core:v10900:10900")
    // 降低依赖权重 避免编译报错
    compileOnly("ink.ptms.core:v12104:12104-minimize:mapped")
    // 版本实现
    compileOnly(project(":project:module-ui:ui-12100"))
    compileOnly(project(":project:module-ui:ui-legacy"))
}