# TIS-3D
TIS-3D is a Minecraft mod inspired by the brilliant game TIS-100 (go buy it if you don't own it yet). It takes the concept of minimal, programmable nodes and expands it to the third dimension, allowing you to build multiblock computers from different modules. Basic modules are the executable module, which can be programmed in very basic ASM (as introduced by TIS-100), and the redstone module, which allows reading and writing redstone signals, enabling basic interaction with the world.

*This mod requires Java 8!*

## License / Use in Modpacks
This mod is [licensed under the **MIT license**](LICENSE). All **assets are public domain**, unless otherwise stated; all are free to be distributed as long as the license / source credits are kept. This means you can use this mod in any mod pack **as you please**. I'd be happy to hear about you using it, though, just out of curiosity.

## Extending
In general, please refer to [the API](src/main/java/li/cil/tis3d/api), everything you need to know should be explained in the Javadoc of the API classes and interfaces.

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
    maven {
        url 'https://cursemaven.com'
        content { includeGroup "curse.maven" }
    }
}
dependencies {
    implementation fg.deobf("curse.maven:tis3d-238603:3553103")
}
```
