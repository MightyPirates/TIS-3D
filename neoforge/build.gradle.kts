val modId: String by project
val minecraftVersion: String = libs.versions.minecraft.get()
val neoforgeVersion: String = libs.versions.neoforge.platform.get()
val neoforgeLoaderVersion: String = libs.versions.neoforge.loader.get()
val architecturyVersion: String = libs.versions.architectury.get()
val manualVersion: String = markdownManualVersion(libs.versions.manual.get())

val gameTestRuntime: Configuration by configurations.creating
val gameTestResultsDir = layout.buildDirectory.dir("test-results/gameTest")
val devOnlyMods: Configuration by configurations.creating
val devOnlyModNames = provider { devOnlyMods.resolvedConfiguration.resolvedArtifacts.map { it.moduleVersion.id.name } }

loom {
    accessWidenerPath.set(project(":common").loom.accessWidenerPath)

    runs {
        named("client") { runDir = "run/client" }
        named("server") { runDir = "run/server" }

        create("gameTestServer") {
            server()
            name("Game Test Server")
            mainClass.set("net.neoforged.fml.startup.GameTestServer")
            runDir = "run/gametest"
            programArgs("--tests", "${modId}_gametest:*")
            property("tis3d.gameTest.junitDir", gameTestResultsDir.get().asFile.absolutePath)
            vmArg("-ea")
        }

        create("clientData") {
            clientData()
            programArgs("--mod", modId)
            programArgs("--output", file("src/generated/resources/").absolutePath)
            programArgs("--existing", project(":common").file("src/main/resources").absolutePath)
            programArgs("--existing", file("src/main/resources").absolutePath)
        }
        create("serverData") {
            serverData()
            programArgs("--mod", modId)
            programArgs("--output", project(":common").file("src/generated/resources").absolutePath)
            programArgs("--existing", project(":common").file("src/main/resources").absolutePath)
            programArgs("--existing", file("src/main/resources").absolutePath)
        }
    }
}

repositories {
    maven("https://maven.neoforged.net/releases")
}

configurations.named("modRuntimeOnly") { extendsFrom(devOnlyMods) }

dependencies {
    neoForge(libs.neoforge.platform)
    modImplementation(libs.neoforge.architectury)

    // Allows `remapSourcesJar` to resolve `@ExpectPlatform` in the common sources it bundles.
    compileOnly(libs.architectury.injectables)

    // Not used by mod, just for dev convenience.
    devOnlyMods(libs.jei.neoforge)

    gameTestRuntime(project(":gametest-neoforge"))

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

val cleanGameTestResults = tasks.register<Delete>("cleanGameTestResults") {
    description = "Deletes game test results and the scratch world from previous runs."
    delete(gameTestResultsDir)
    delete(layout.projectDirectory.dir("run/gametest/gametestserver"))
    delete(layout.projectDirectory.dir("run/gametest/world"))
}

tasks.named<JavaExec>("runGameTestServer") {
    dependsOn(cleanGameTestResults)
    classpath += gameTestRuntime
    classpath = classpath.filter { file -> devOnlyModNames.get().none { file.name.startsWith("${it}-") } }
}
