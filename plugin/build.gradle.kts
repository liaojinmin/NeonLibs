import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

val kotlinVersionNum: String
    get() = project.kotlin.coreLibrariesVersion.replace(".", "")

val generatedYamlDir = File("$buildDir/generated")

dependencies {
    implementation(project(":common-env"))
    implementation(project(":common-script"))
    implementation(project(":common"))
    implementation(project(":project:module-hook"))
 //   implementation(project(":project:module-smtp"))
    implementation(project(":project:module-common"))
    implementation(project(":project:module-nms"))
    implementation(project(":project:module-nms-data-serializer"))
    implementation(project(":project:module-nms-data-serializer:nms-data-serializer-12005"))
    implementation(project(":project:module-nms-data-serializer:nms-data-serializer-legacy"))
    implementation(project(":project:module-nms-util"))
    implementation(project(":project:module-nms-carrier"))
    implementation(project(":project:module-configuration"))
    implementation(project(":project:module-effect"))
}

tasks {

    withType<ShadowJar> {
        this.archiveFileName.set("${rootProject.name}-${rootProject.version}.jar")
        archiveClassifier.set("")
        exclude("META-INF/maven/**")
        exclude("META-INF/tf/**")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/gfprobe-provider.xml")
        exclude("META-INF/NOTICE.txt")
        exclude("com/google/**")
        exclude("org/apache/**")
        exclude("org/slf4j/**")
        exclude("org/json/**")
        exclude("org/intellij/**")
        exclude("org/jetbrains/**")
        // 不打包 Kotlin
        //exclude("kotlin/**")

        // 重定向 Kotlin
        //relocate("kotlin.", "kotlin${kotlinVersionNum}.") {
            //exclude("kotlin.Metadata")
        //}

        // luck
        relocate("me.lucko.jarrelocator","${rootProject.group}.libraries.relocator" )

        // nbt
        relocate("de.tr7zw.changeme.nbtapi","${rootProject.group}.libraries.nbt" )

        // asm
        relocate("org.objectweb.asm", "${rootProject.group}.libraries.asm")

        // tabooproject reflex
        relocate("org.tabooproject.reflex", "${rootProject.group}.libraries.reflex")

        // hikari
        relocate("com.zaxxer.hikari", "${rootProject.group}.libraries.zaxxer.hikari")
        // slf4j ’hikari‘
        relocate("org.slf4j", "${rootProject.group}.libraries.org.slf4j")

        // nightconfig
        relocate("com.electronwill.nightconfig.core.conversion", "${rootProject.group}.libraries.configuration")
        relocate("com.electronwill.nightconfig", "com.electronwill.nightconfig_3_6_7")
        // snakeyaml
        relocate("org.yaml.snakeyaml", "org.yaml.snakeyaml_2_2")

        classifier = null
    }

    shadowJar {
        println("> Apply plugin.yml")
        dependsOn("generateYaml")
        from(generatedYamlDir)
    }

    build {
        dependsOn(shadowJar)
    }

    project.publishing {
        publications {
            create<MavenPublication>("maven") {
                artifactId = rootProject.name
                groupId = "me.neon.libs"
                version = (if (project.hasProperty("build")) {
                    var build = project.findProperty("build").toString()
                    if (build.startsWith("task ")) {
                        build = "local"
                    }
                    "${project.version}-$build"
                } else {
                    "${project.version}"
                })
                artifact(shadowJar)
                println("> Apply \"$groupId:$artifactId:$version\"")
            }
        }
    }
}

tasks.register("generateYaml") {
    doLast {
        if (!generatedYamlDir.exists()) {
            generatedYamlDir.mkdirs()
        }
        val yamlContent = """
            name: ${rootProject.name}
            version: ${project.version}
            main:  ${project.group}.NeonLibsLoader
            load: STARTUP
            authors:
              - '老廖'
            softdepend:
              - Vault
              - PlaceholderAPI
              - PlayerPoints
            api-version: 1.13
        """.trimIndent()
        val outputFile = File("$generatedYamlDir/plugin.yml")
        outputFile.writeText(yamlContent)
    }
}

gradle.buildFinished {
    File(buildDir, "libs/${project.name}-${rootProject.version}.jar").delete()
    File(buildDir, "libs/plugin-${rootProject.version}-sources.jar").delete()
}


