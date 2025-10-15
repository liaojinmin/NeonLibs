dependencies {
    compileOnly(project(":common-env"))
    compileOnly(project(":common"))
    compileOnly(project(":project:module-common"))
    compileOnly(project(":project:module-configuration"))
    compileOnly("ink.ptms.core:v11604:11604")
    compileOnly("org.openjdk.nashorn:nashorn-core:15.6") {
        exclude(group = "org.ow2.asm")
    }
}