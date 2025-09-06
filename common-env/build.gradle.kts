dependencies {

    implementation("me.lucko:jar-relocator:1.5") {
        exclude(group = "org.ow2.asm")
    }

    implementation("de.tr7zw:item-nbt-api:2.15.2")
    implementation("org.ow2.asm:asm:9.6")
    implementation("org.ow2.asm:asm-util:9.6")
    implementation("org.ow2.asm:asm-commons:9.6")

    implementation("org.tabooproject.reflex:reflex:1.2.0") {
        exclude(group = "org.ow2.asm")
    }

    implementation("org.tabooproject.reflex:analyser:1.2.0") {
        exclude(group = "org.ow2.asm")
    }

}

