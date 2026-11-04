val modId = providers.gradleProperty("modId").get()
val minecraftVersion: String = libs.versions.minecraft.get()
val neoforgeVersion: String = libs.versions.neoforge.platform.get()
val neoforgeLoaderVersion: String = libs.versions.neoforge.loader.get()
val architecturyVersion: String = libs.versions.architectury.get()
val manualVersion: String = markdownManualVersion(libs.versions.manual.get())

val gameTestRuntime = configurations.create("gameTestRuntime")
val gameTestResultsDir = layout.buildDirectory.dir("test-results/gameTest")
val devOnlyMods = configurations.create("devOnlyMods")
val devOnlyModNames = provider { devOnlyMods.resolvedConfiguration.resolvedArtifacts.map { it.moduleVersion.id.name } }

sourceSets.main {
    resources.srcDir("src/generated/client")
    resources.srcDir("src/generated/server")
    resources.exclude(".cache/**")
}

loom {
    accessWidenerPath.set(project(":common").loom.accessWidenerPath)

    runs {
        named("client") { runDirectory.set(file("run/client")) }
        named("server") { runDirectory.set(file("run/server")) }

        create("gameTestServer") {
            server()
            name("Game Test Server")
            mainClass.set("net.neoforged.fml.startup.GameTestServer")
            runDirectory.set(file("run/gametest"))
            programArguments.addAll("--tests", "${modId}_gametest:*")
            systemProperties.put("tis3d.gameTest.junitDir", gameTestResultsDir.get().asFile.absolutePath)
            jvmArguments.add("-ea")
        }

        create("clientData") {
            clientData()
            programArguments.addAll("--mod", modId)
            programArguments.addAll("--output", file("src/generated/client").absolutePath)
            programArguments.addAll("--existing", project(":common").file("src/main/resources").absolutePath)
            programArguments.addAll("--existing", file("src/main/resources").absolutePath)
        }
        create("serverData") {
            serverData()
            programArguments.addAll("--mod", modId)
            programArguments.addAll("--output", file("src/generated/server").absolutePath)
            programArguments.addAll("--existing", project(":common").file("src/main/resources").absolutePath)
            programArguments.addAll("--existing", file("src/main/resources").absolutePath)
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
        modImplementation(files(markdownManualJar("neoforge", "neoforge-*.jar")))
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
