val modId: String by project
val minecraftVersion: String = libs.versions.minecraft.get()
val fabricApiVersion: String = libs.versions.fabric.api.get()
val architecturyVersion: String = libs.versions.architectury.get()
val forgeConfigPortVersion: String = libs.versions.fabric.forgeConfigPort.get()
val manualVersion: String = libs.versions.manual.get()

loom {
    accessWidenerPath.set(project(":common").loom.accessWidenerPath)

    runs {
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

    modImplementation(libs.fabric.manual)
    modImplementation(libs.fabric.forgeConfigPort)

    // Not used by mod, just for dev convenience.
    modRuntimeOnly(libs.fabric.tooltipFix)
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
