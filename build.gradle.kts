import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.configurationcache.extensions.capitalized

plugins {
    java
    alias(libs.plugins.architectury)
    alias(libs.plugins.loom) apply false
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.spotless)
}

val modId: String by project
val modVersion: String by project
val mavenGroup: String by project
val enabledPlatforms: String by project
val minecraftVersion: String = libs.versions.minecraft.get()

fun getGitRef(): String {
    return providers.exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
        isIgnoreExitValue = true
    }.standardOutput.asText.get().trim()
}

subprojects {
    apply(plugin = "java")
    apply(plugin = rootProject.libs.plugins.architectury.get().pluginId)
    apply(plugin = rootProject.libs.plugins.loom.get().pluginId)

    version = "${modVersion}+${getGitRef()}"
    group = mavenGroup
    base.archivesName.set("${modId}-MC${minecraftVersion}-${project.name}")

    architectury {
        minecraft = minecraftVersion
    }

    configure<LoomGradleExtensionAPI> {
        silentMojangMappingsLicense()
    }

    repositories {
        exclusiveContent {
            forRepository { maven("https://maven.parchmentmc.org") }
            filter { includeGroupByRegex("org\\.parchmentmc.*") }
        }
        exclusiveContent {
            forRepository { maven("https://cursemaven.com") }
            filter { includeGroup("curse.maven") }
        }
        exclusiveContent {
            forRepository { maven("https://api.modrinth.com/maven") }
            filter { includeGroup("maven.modrinth") }
        }
        exclusiveContent {
            forRepository { flatDir { dir("${rootProject.projectDir}/libs") } }
            filter { includeGroup("local") }
        }
    }

    dependencies {
        "minecraft"(rootProject.libs.minecraft)
        val loom = project.extensions.getByName<LoomGradleExtensionAPI>("loom")
        "mappings"(loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-$minecraftVersion:${rootProject.libs.versions.parchment.get()}@zip")
        })
        "compileOnly"("com.google.code.findbugs:jsr305:3.0.2")
    }

    tasks {
        jar {
            from("LICENSE") {
                rename { "${it}_${modId}" }
            }
        }

        withType<JavaCompile>().configureEach {
            options.encoding = "utf-8"
            options.release.set(17)
        }
    }

    idea {
        module {
            for (exclude in arrayOf("out", "logs")) {
                excludeDirs.add(file(exclude))
            }
        }
    }
}

for (platform in enabledPlatforms.split(',')) {
    project(":$platform") {
        apply(plugin = rootProject.libs.plugins.shadow.get().pluginId)

        architectury {
            platformSetupLoomIde()
            loader(platform)
        }

        val common: Configuration by configurations.creating
        val shadowCommon: Configuration by configurations.creating

        configurations {
            compileClasspath.get().extendsFrom(common)
            runtimeClasspath.get().extendsFrom(common)
            getByName("development${platform.capitalized()}").extendsFrom(common)
        }

        dependencies {
            common(project(path = ":common", configuration = "namedElements")) { isTransitive = false }
            shadowCommon(project(path = ":common", configuration = "transformProduction${platform.capitalized()}")) { isTransitive = false }
        }

        tasks {
            withType<ShadowJar> {
                exclude("architectury.common.json")
                configurations = listOf(shadowCommon)
                archiveClassifier.set("dev-shadow")
            }

            withType<RemapJarTask> {
                val shadowJarTask = getByName<ShadowJar>("shadowJar")
                inputFile.set(shadowJarTask.archiveFile)
                dependsOn(shadowJarTask)
                archiveClassifier.set(null as String?)
            }

            jar {
                archiveClassifier.set("dev")
            }
        }

        (components["java"] as AdhocComponentWithVariants)
            .withVariantsFromConfiguration(configurations["shadowRuntimeElements"]) {
                skip()
            }
    }
}

spotless {
    java {
        target("*/src/*/java/li/cil/**/*.java")

        endWithNewline()
        trimTrailingWhitespace()
        removeUnusedImports()
        indentWithSpaces()
    }
}
