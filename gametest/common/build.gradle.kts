val enabledPlatforms = providers.gradleProperty("enabledPlatforms").get()

architectury {
    common(enabledPlatforms.split(","))
}

dependencies {
    modImplementation(libs.fabric.loader)
    modApi(libs.architectury.api)

    compileOnly(project(path = ":common", configuration = "namedElements"))
}
