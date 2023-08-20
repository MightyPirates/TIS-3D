pluginManagement {
    repositories {
        exclusiveContent {
            forRepository { maven("https://maven.architectury.dev") }
            filter {
                includeGroup("architectury-plugin")
                includeGroupByRegex("dev\\.architectury.*")
            }
        }
        exclusiveContent {
            forRepository { maven("https://maven.fabricmc.net") }
            filter {
                includeGroup("net.fabricmc")
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
include("fabric")
include("forge")

val modId: String by settings
rootProject.name = modId
