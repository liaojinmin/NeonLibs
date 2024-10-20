
repositories {
    maven {
        url = uri("'https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
}
dependencies {
    compileOnly("net.kyori:adventure-api:4.9.2")

    compileOnly(project(":common"))
    compileOnly(project(":common-env"))
    compileOnly(project(":project:module-nms"))

}