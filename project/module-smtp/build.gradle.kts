dependencies {
    implementation("javax.mail:javax.mail-api:1.6.2")

    compileOnly(project(":common"))
    compileOnly(project(":common-env"))

    implementation("javax.mail:mail:1.5.0-b01")
    implementation("javax.activation:activation:1.1.1")

}