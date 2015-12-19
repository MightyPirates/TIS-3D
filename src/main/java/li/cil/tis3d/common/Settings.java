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

        maxCasingsPerController = config.getInt("maxCasings", "controller",
                Settings.maxCasingsPerController, 1, 512,
                "The maximum number of casings a single controller supports.");

        maxLinesPerProgram = config.getInt("maxLinesPerProgram", "module.execution",
                Settings.maxLinesPerProgram, 1, 200,
                "The maximum number of lines an ASM program for an execution node may have.");
        maxColumnsPerLine = config.getInt("maxColumnsPerLine", "module.execution",
                Settings.maxColumnsPerLine, 1, 80,
                "The maximum number of columns per line of an ASM program for an execution node may have.");
        maxInfraredQueueLength = config.getInt("maxQueueLength", "module.infrared",
                Settings.maxInfraredQueueLength, 1, 64,
                "The maximum number of infrared packets that can be stored in the receiver's buffer.");

        disabledModules.clear();
        checkModule(config, "module.audio", Constants.NAME_ITEM_MODULE_AUDIO);
        checkModule(config, "module.bundledRedstone", Constants.NAME_ITEM_MODULE_BUNDLED_REDSTONE);
        checkModule(config, "module.display", Constants.NAME_ITEM_MODULE_DISPLAY);
        checkModule(config, "module.execution", Constants.NAME_ITEM_MODULE_EXECUTION);
        checkModule(config, "module.infrared", Constants.NAME_ITEM_MODULE_INFRARED);
        checkModule(config, "module.random", Constants.NAME_ITEM_MODULE_RANDOM);
        checkModule(config, "module.redstone", Constants.NAME_ITEM_MODULE_REDSTONE);
        checkModule(config, "module.stack", Constants.NAME_ITEM_MODULE_STACK);

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
