package li.cil.tis3d.common;

import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * User configurable stuff via config file.
 */
public final class Settings {
    /**
     * The maximum number of packets to allow sending per tick before
     * throttling kicks in, killing duplicate data packets.
     */
    public static int maxPacketsPerTick = 10;

    /**
     * The maximum number of particle effects to allow sending per tick
     * before throttling kicks in, killing duplicate effects.
     */
    public static int maxParticlesPerTick = 5;

    /**
     * The maximum number of casings that may be connected to a controller.
     */
    public static int maxCasingsPerController = 8;

    /**
     * The maximum number of lines a piece of code may have.
     */
    public static int maxLinesPerProgram = 20;

    /**
     * The maximum number of characters a single line may have.
     */
    public static int maxColumnsPerLine = 18;

    /**
     * Maximum number of items stored in our receiver queue.
     * <p>
     * If the queue runs full, additionally received packets will be dropped.
     */
    public static int maxInfraredQueueLength = 16;

    /**
     * The list of <em>disabled</em> modules. Disabled modules will not be
     * registered with the game. Filled in while loading, for convenience.
     */
    public static final Set<String> disabledModules = new HashSet<>();

    // --------------------------------------------------------------------- //

    public static void load(final File configFile) {
        final Configuration config = new Configuration(configFile);

        config.load();

        maxPacketsPerTick = config.getInt("maxPacketsPerTick", "network",
                maxPacketsPerTick, 1, 500,
                "The maximum number of status packets modules may send per tick. When this is exceeded, throttling kicks in.");
        maxParticlesPerTick = config.getInt("maxParticlesPerTick", "network",
                maxParticlesPerTick, 1, 500,
                "The maximum number of particle effects data transfer may trigger per tick. When this is exceeded, throttling kicks in.");

        maxCasingsPerController = config.getInt("maxCasings", "controller",
                maxCasingsPerController, 1, 512,
                "The maximum number of casings a single controller supports.");

        maxLinesPerProgram = config.getInt("maxLinesPerProgram", "module.execution",
                maxLinesPerProgram, 1, 200,
                "The maximum number of lines an ASM program for an execution node may have.");
        maxColumnsPerLine = config.getInt("maxColumnsPerLine", "module.execution",
                maxColumnsPerLine, 1, 80,
                "The maximum number of columns per line of an ASM program for an execution node may have.");
        maxInfraredQueueLength = config.getInt("maxQueueLength", "module.infrared",
                maxInfraredQueueLength, 1, 64,
                "The maximum number of infrared packets that can be stored in the receiver's buffer.");

        // Rebuild list of disabled modules.
        disabledModules.clear();
        // Strip module and first letter from internal name, lowercase first letter.
        final int prefixLength = "module*".length();
        for (final String module : Constants.MODULES) {
            final String name = String.valueOf(module.charAt(prefixLength - 1)).toLowerCase() + module.substring(prefixLength);
            checkModule(config, "module." + name, module);
        }

        if (config.hasChanged()) {
            config.save();
        }
    }

    private static void checkModule(final Configuration config, final String path, final String name) {
        if (!config.getBoolean("enabled", path, true,
                "Whether the module is enabled. Disabled modules are not registered, meaning if you disable them later on the items will disappear!")) {
            disabledModules.add(name);
        }
    }

    // --------------------------------------------------------------------- //

    private Settings() {
    }
}
