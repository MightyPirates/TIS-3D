pluginManagement {
    repositories {
        exclusiveContent {
            forRepository { maven("https://maven.architectury.dev") }
            filter {
                includeGroup("architectury-plugin")
                includeGroupByRegex("dev\\.architectury.*")
                includeGroup("com.mojang")
            }
        }
        exclusiveContent {
            forRepository { maven("https://maven.fabricmc.net") }
            filter {
                includeGroupByRegex("net\\.fabricmc.*")
                includeGroup("fabric-loom")
            }
        }
        exclusiveContent {
            forRepository { maven("https://maven.minecraftforge.net") }
            filter {
                includeGroupByRegex("net\\.minecraftforge.*")
                includeGroup("de.oceanlabs.mcp")
            }
        }
        gradlePluginPortal()
    }
}

include("common")

val enabledPlatforms = providers.gradleProperty("enabledPlatforms").get()
for (enabledPlatform in enabledPlatforms.split(",")) {
    include(enabledPlatform)
}

for (module in listOf("common") + enabledPlatforms.split(",")) {
    include("gametest-$module")
    project(":gametest-$module").projectDir = file("gametest/$module")
}

val modId = providers.gradleProperty("modId").get()
rootProject.name = modId
