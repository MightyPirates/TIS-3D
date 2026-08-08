val modId: String by project
val minecraftVersion: String = libs.versions.minecraft.get()
val neoforgeVersion: String = libs.versions.neoforge.platform.get()
val neoforgeLoaderVersion: String = libs.versions.neoforge.loader.get()
val architecturyVersion: String = libs.versions.architectury.get()
val markdownManualDir: String by project

loom {
    accessWidenerPath.set(project(":common").loom.accessWidenerPath)

    runs {
        create("data") {
            data()
            programArgs("--all")
            programArgs("--mod", modId)
            programArgs("--output", file("src/generated/resources/").absolutePath)
            programArgs("--existing", project(":common").file("src/main/resources").absolutePath)
            programArgs("--existing", file("src/main/resources").absolutePath)
        }
    }
}

repositories {
    maven("https://maven.neoforged.net/releases")
}

val useLocalMarkdownManual = rootProject.file(markdownManualDir).isDirectory

fun markdownManualJar(module: String, pattern: String): File =
    fileTree(rootProject.file("$markdownManualDir/$module/build/libs")) {
        include(pattern)
        exclude("*-dev-shadow.jar", "*-sources.jar")
    }.files.maxByOrNull { it.lastModified() } ?: error("No jar matching '$pattern' in $markdownManualDir/$module/build/libs; build that project first.")

val manualVersion: String = if (useLocalMarkdownManual) "0.0.0" else libs.versions.manual.get()

dependencies {
    neoForge(libs.neoforge.platform)
    modImplementation(libs.neoforge.architectury)

    if (useLocalMarkdownManual) {
        modImplementation(files(markdownManualJar("neoforge", "markdown_manual-MC*-neoforge-*.jar")))
    } else {
        modImplementation(libs.neoforge.manual)
    }
}

tasks {
    processResources {
        val properties = mapOf(
            "version" to project.version,
            "minecraftVersion" to minecraftVersion,
            "loaderVersion" to neoforgeLoaderVersion,
            "neoforgeVersion" to neoforgeVersion,
            "architecturyVersion" to architecturyVersion,
            "manualVersion" to manualVersion
        )
        inputs.properties(properties)
        filesMatching("META-INF/neoforge.mods.toml") {
            expand(properties)
        }
    }

    remapJar {
        atAccessWideners.add("${modId}.accesswidener")
    }
}
