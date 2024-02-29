
repositories {
    maven {
        url = uri("'https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
}
dependencies {
    compileOnly("net.kyori:adventure-api:4.9.2")
    compileOnly("me.clip:placeholderapi:2.10.0")

    compileOnly("org.ow2.asm:asm:9.4")
    compileOnly("org.ow2.asm:asm-util:9.4")
    compileOnly("org.ow2.asm:asm-commons:9.4")
    compileOnly("org.tabooproject.reflex:reflex:1.0.19")
    compileOnly("org.tabooproject.reflex:analyser:1.0.19")
    compileOnly("org.openjdk.nashorn:nashorn-core:15.4")

    compileOnly(kotlin("reflect"))
    compileOnly(project(":common-env"))


}