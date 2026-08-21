val modId: String by project
val minecraftVersion: String = libs.versions.minecraft.get()
val fabricApiVersion: String = libs.versions.fabric.api.get()
val architecturyVersion: String = libs.versions.architectury.get()
val forgeConfigPortVersion: String = libs.versions.fabric.forgeConfigPort.get()
val manualVersion: String = markdownManualVersion(libs.versions.manual.get())

val gameTestRuntime: Configuration by configurations.creating
val gameTestResultsDir = layout.buildDirectory.dir("test-results/gameTest")

loom {
    accessWidenerPath.set(project(":common").loom.accessWidenerPath)

    runs {
        named("client") { runDir = "run/client" }
        named("server") { runDir = "run/server" }

        create("gameTest") {
            server()
            runDir = "run/gametest"
            vmArg("-Dfabric-api.gametest")
            vmArg("-Dfabric-api.gametest.report-file=${gameTestResultsDir.get().asFile.absolutePath}/fabric-game-tests.xml")
            vmArg("-ea")
        }

        create("data") {
            client()
            name("Data Generation")
            vmArg("-Dfabric-api.datagen")
            vmArg("-Dfabric-api.datagen.output-dir=${file("src/generated/resources")}")
            vmArg("-Dfabric-api.datagen.modid=${modId}")
            vmArg("-Dfabric-api.datagen.strict-validation")

            runDir("build/datagen")
        }
    }
}

val cleanGameTestResults = tasks.register<Delete>("cleanGameTestResults") {
    description = "Deletes game test results and the scratch world from previous runs."
    delete(gameTestResultsDir)
    delete(layout.projectDirectory.dir("run/gametest/world"))
}

val fixGameTestReport = tasks.register("fixGameTestReport") {
    val reportFile = gameTestResultsDir.map { it.file("fabric-game-tests.xml") }
    outputs.upToDateWhen { false }
    doLast {
        normalizeGameTestReport(reportFile.get().asFile)
    }
}

tasks.named<JavaExec>("runGameTest") {
    dependsOn(cleanGameTestResults)
    classpath += gameTestRuntime
    finalizedBy(fixGameTestReport)
}

repositories {
    exclusiveContent {
        forRepository { maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/") }
        filter { includeGroup("fuzs.forgeconfigapiport") }
    }
    exclusiveContent {
        forRepository { maven("https://maven.shedaniel.me/") }
        filter { includeGroupByRegex("me\\.shedaniel.*") }
    }
}

dependencies {
    modImplementation(libs.fabric.loader)
    modApi(libs.fabric.api)
    modApi(libs.fabric.architectury)

    // Optional integration, see the `rei_client` entrypoint; compile against the API only.
    modCompileOnly(libs.fabric.roughlyEnoughItems.api)
    modRuntimeOnly(libs.fabric.roughlyEnoughItems)

    if (useLocalMarkdownManual) {
        modImplementation(files(markdownManualJar("fabric", "markdown_manual-MC*-fabric-*.jar")))
    } else {
        modImplementation(libs.fabric.manual)
    }
    modImplementation(libs.fabric.forgeConfigPort)

    // Not used by mod, just for dev convenience.
    modRuntimeOnly(libs.fabric.tooltipFix)

    // Only the game test run gets the game test mod, so runClient and runServer never load it.
    gameTestRuntime(project(path = ":gametest-fabric", configuration = "namedElements")) { isTransitive = false }

}

tasks {
    processResources {
        val properties = mapOf(
            "version" to project.version,
            "minecraftVersion" to minecraftVersion,
            "fabricApiVersion" to fabricApiVersion,
            "architecturyVersion" to architecturyVersion,
            "forgeConfigPortVersion" to forgeConfigPortVersion,
            "manualVersion" to manualVersion
        )
        inputs.properties(properties)
        filesMatching("fabric.mod.json") {
            expand(properties)
        }
    }

    remapJar {
        injectAccessWidener.set(true)
    }
}
