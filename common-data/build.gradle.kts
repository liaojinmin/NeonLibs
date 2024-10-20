repositories {
    maven {
        url = uri("'https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
}
dependencies {

    compileOnly(project(":common"))
    compileOnly(project(":common-env"))

}

