package li.cil.tis3d.common;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

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

        if (config.hasChanged()) {
            config.save();
        }
    }

    // --------------------------------------------------------------------- //

    private Settings() {
    }
}
