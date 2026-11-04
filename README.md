# TIS-3D

[![build](https://img.shields.io/github/actions/workflow/status/MightyPirates/TIS-3D/build.yml?label=build)](https://github.com/MightyPirates/TIS-3D/actions/workflows/build.yml)
[![game tests](https://img.shields.io/github/actions/workflow/status/MightyPirates/TIS-3D/test-report.yml?label=game%20tests)](https://github.com/MightyPirates/TIS-3D/actions/workflows/test-report.yml)
[![curseforge](https://img.shields.io/curseforge/dt/238603?label=curseforge&color=f16436&logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/tis-3d)
[![modrinth](https://img.shields.io/modrinth/dt/tis3d?label=modrinth&color=1bd96a&logo=modrinth&logoColor=white)](https://modrinth.com/mod/tis3d)
![loaders](https://img.shields.io/badge/loaders-Fabric%20%7C%20NeoForge-blueviolet)

TIS-3D is a Minecraft mod inspired by the brilliant game TIS-100 (go buy it if you don't own it yet). It takes the concept of minimal, programmable nodes and expands it to the third dimension, allowing you to build multiblock computers from different modules. Basic modules are the executable module, which can be programmed in very basic ASM (as introduced by TIS-100), and the redstone module, which allows reading and writing redstone signals, enabling basic interaction with the world.

## License / Use in Modpacks
This mod is [licensed under the **MIT license**](LICENSE). All **assets are public domain**, unless otherwise stated; all are free to be distributed as long as the license / source credits are kept. This means you can use this mod in any mod pack **as you please**.

## Extending
In general, please refer to [the API](common/src/main/java/li/cil/tis3d/api), everything you need to know should be explained in the Javadoc of the API classes and interfaces.

There are two main ways of extending TIS-3D: by adding custom modules, and by adding serial protocols for the serial port module. Create a custom module if it has its own, self-contained functionality, such as the display module for example.

When adding integration with another mod, I strongly ask that you first consider whether this can already be achieved via redstone. If it can, even if it's slightly more complicated, stop right there. If it can't, there are the aforementioned two options, module or protocol.

The general rule of thumb should be this:
- if you're adding integration for a general *concept*, usually support for some interface implemented by numerous blocks, such as Minecraft's `IInventory`, use a *module*. 
- if you're adding integration for one specific *block*, such as Minecraft's command block, use a *protocol* so that the block can be communicated with using the serial port module.

The rationale here being that in the former case the serial protocol is explicitly defined by the *module*, whereas in the latter case the serial protocol is explicitly defined by the *block*. Again, if at all possible, prefer avoiding either of the two and use redstone (and comparators) instead.

### Gradle
To add a dependency to TIS-3D for use in your mod, add the following to your `build.gradle`:

```groovy
repositories {
    exclusiveContent {
        forRepository { maven("https://api.modrinth.com/maven") }
        filter { includeGroup("maven.modrinth") }
    }
}
dependencies {
    // Fabric
    modImplementation("maven.modrinth:tis3d:MC1.21.1-fabric-1.2.6")
    // NeoForge
    modImplementation("maven.modrinth:tis3d:MC1.21.1-neoforge-1.2.6")
}
```

The version is `MC<minecraft version>-<loader>-<mod version>`, matching the release names on
[Modrinth](https://modrinth.com/mod/tis3d). To compile against the API only, without pulling in the
mod itself, use the `-api` jar attached to the [GitHub release](https://github.com/MightyPirates/TIS-3D/releases).
