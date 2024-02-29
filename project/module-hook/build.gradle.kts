val taboolibVersion: String by project

dependencies {
    compileOnly(fileTree("libs"))

    compileOnly(project(":project:module-common"))
    compileOnly("org.black_ixx:playerpoints:3.1.1")
    compileOnly("com.github.MilkBowl:VaultAPI:-SNAPSHOT") { isTransitive = false }
    compileOnly("me.clip:placeholderapi:2.10.9") { isTransitive = false }
}