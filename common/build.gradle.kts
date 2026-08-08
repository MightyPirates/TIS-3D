val enabledPlatforms: String by project
val modId: String by project
val markdownManualDir: String by project

architectury {
    common(enabledPlatforms.split(","))
}

loom {
    accessWidenerPath.set(file("src/main/resources/${modId}.accesswidener"))
}

val useLocalMarkdownManual = rootProject.file(markdownManualDir).isDirectory

fun markdownManualJar(module: String, pattern: String): File =
    fileTree(rootProject.file("$markdownManualDir/$module/build/libs")) {
        include(pattern)
        exclude("*-dev-shadow.jar", "*-sources.jar")
    }.files.maxByOrNull { it.lastModified() } ?: error("No jar matching '$pattern' in $markdownManualDir/$module/build/libs; build that project first.")

dependencies {
    modImplementation(libs.fabric.loader)
    modApi(libs.architectury.api)

    if (useLocalMarkdownManual) {
        compileOnly(files(markdownManualJar("common", "markdown_manual-MC*-common-*-api.jar")))
    } else {
        modApi(libs.fabric.manual)
    }
}

tasks {
    register<Jar>("apiJar") {
        from(sourceSets.main.get().allSource)
        from(sourceSets.main.get().output)
        archiveClassifier.set("api")
        include("li/cil/${modId}/api/**")
    }

    jar {
        dependsOn("apiJar")
    }
}
