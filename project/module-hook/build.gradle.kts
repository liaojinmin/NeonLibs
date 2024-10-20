
dependencies {
    compileOnly(fileTree("libs"))

    compileOnly(project(":project:module-common"))
    compileOnly("org.black_ixx:playerpoints:3.1.1")
    compileOnly("me.clip:placeholderapi:2.10.9") { isTransitive = false }
}