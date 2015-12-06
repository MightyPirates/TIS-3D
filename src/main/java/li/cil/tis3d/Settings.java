package li.cil.tis3d;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

/**
 * User configurable stuff via config file.
 */
public final class Settings {
    /**
     * The maximum number of lines a piece of code may have.
     */
    public static int maxLinesPerProgram = 20;

    /**
     * The maximum number of characters a single line may have.
     */
    public static int maxColumnsPerLine = 18;

    // --------------------------------------------------------------------- //

    public static void load(final File configFile) {
        final Configuration config = new Configuration(configFile);

        config.load();

        config.getInt("maxLinesPerProgram", "Module.Execution", Settings.maxLinesPerProgram, 1, 200,
                "The maximum number of lines an ASM program for an execution node may have.");
        config.getInt("maxColumnsPerLine", "Module.Execution", Settings.maxColumnsPerLine, 1, 80,
                "The maximum number of columns per line of an ASM program for an execution node may have.");

        if (config.hasChanged()) {
            config.save();
        }
    }

    // --------------------------------------------------------------------- //

    private Settings() {
    }
}
