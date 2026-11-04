val enabledPlatforms = providers.gradleProperty("enabledPlatforms").get()
val modId = providers.gradleProperty("modId").get()

architectury {
    common(enabledPlatforms.split(","))
}

loom {
    accessWidenerPath.set(file("src/main/resources/${modId}.accesswidener"))
}

dependencies {
    modImplementation(libs.fabric.loader)
    modApi(libs.architectury.api)

    if (useLocalMarkdownManual) {
        compileOnly(files(markdownManualJar(".", "*-api.jar")))
    } else {
        compileOnly(libs.common.manual.api)
    }
}
