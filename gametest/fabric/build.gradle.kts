dependencies {
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
    modImplementation(libs.fabric.architectury)

    compileOnly(project(path = ":common", configuration = "namedElements"))
}
