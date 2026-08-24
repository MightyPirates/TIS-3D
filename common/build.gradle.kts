val enabledPlatforms: String by project
val modId: String by project

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
        compileOnly(files(markdownManualJar(".", "markdown_manual-MC*-api.jar")))
    } else {
        compileOnly(libs.common.manual.api)
    }
}
