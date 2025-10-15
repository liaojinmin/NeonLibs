import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    compileOnly(project(":common-env"))
    compileOnly(project(":common"))

    compileOnly(project(":project:module-common"))
    //compileOnly("ink.ptms.core:v11604:11604")
    compileOnly("ink.ptms.core:v12002:12002-minimize:mapped")

    // 基本库
    implementation("org.yaml:snakeyaml:2.2")
  //  implementation("com.typesafe:config:1.4.3")
    implementation("com.electronwill.night-config:core:3.6.7")
    implementation("com.electronwill.night-config:toml:3.6.7")
    implementation("com.electronwill.night-config:json:3.6.7")
    implementation("com.electronwill.night-config:hocon:3.6.7")
    implementation("com.electronwill.night-config:core-conversion:6.0.0")


}

repositories {
    mavenCentral()
}
