package li.cil.tis3d.common;

import li.cil.tis3d.util.SimpleConfig;
import net.minecraft.util.Identifier;

import java.io.File;
import java.util.HashSet;
import java.util.Locale;
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
     * The maximum number of lines a program may have.
     */
    public static int maxLinesPerProgram = 40;

    /**
     * The maximum number of characters a single line in a program may have.
     */
    public static int maxColumnsPerLine = 29;

    /**
     * Maximum number of items stored in our receiver queue.
     * <p>
     * If the queue runs full, additionally received packets will be dropped.
     */
    public static int maxInfraredQueueLength = 16;

    /**
     * Whether to swing the player's arm while typing in a terminal module.
     */
    public static boolean animateTypingHand = true;

    /**
     * The list of <em>disabled</em> modules. Disabled modules will not be
     * registered with the game. Filled in while loading, for convenience.
     */
    public static final Set<Identifier> disabledModules = new HashSet<>();

    // --------------------------------------------------------------------- //

    private static final String NAME_ANIMATE_TYPING = "module.terminal.animate_typing";
    private static final String NAME_MAX_PACKETS_PER_TICK = "network.max_packets_per_tick";
    private static final String NAME_MAX_PARTICLES_PER_TICK = "network.max_particles_per_tick";
    private static final String NAME_MAX_CASINGS_PER_CONTROLLER = "controller.max_casings";
    private static final String NAME_MAX_LINES_PER_PROGRAM = "module.execution.max_lines_per_program";
    private static final String NAME_MAX_COLUMNS_PER_LINE = "module.execution.max_columns_per_line";
    private static final String NAME_MAX_QUEUE_LENGTH = "module.infrared.max_queue_length";
    private static final String NAME_MODULE_ENABLED_PATTERN = "module.%s.enabled";

    private static final String COMMENT_ANIMATE_TYPING = "Whether to swing the player's arm while typing on a terminal module.";
    private static final String COMMENT_MAX_PACKETS_PER_TICK = "The maximum number of status packets modules may send per tick. When this is exceeded, throttling kicks in.";
    private static final String COMMENT_MAX_PARTICLES_PER_TICK = "The maximum number of particle effects data transfer may trigger per tick. When this is exceeded, throttling kicks in.";
    private static final String COMMENT_MAX_CASINGS_PER_CONTROLLER = "The maximum number of casings a single controller supports.";
    private static final String COMMENT_MAX_LINES_PER_PROGRAM = "The maximum number of lines an ASM program for an execution node may have.";
    private static final String COMMENT_MAX_COLUMNS_PER_LINE = "The maximum number of columns per line of an ASM program for an execution node may have.";
    private static final String COMMENT_MAX_QUEUE_LENGTH = "The maximum number of infrared packets that can be stored in the receiver's buffer.";
    private static final String COMMENT_MODULE_ENABLED_PATTERN = "Whether the %s module is enabled. Disabled modules are not registered, meaning if you disable them later on the items will disappear!";

    // --------------------------------------------------------------------- //

    @SuppressWarnings("ConstantConditions")
    public static void load(final File configFile) {
        final SimpleConfig config = SimpleConfig.create(configFile);


        maxPacketsPerTick = config.getInt(NAME_MAX_PACKETS_PER_TICK, maxPacketsPerTick, 1, 500, COMMENT_MAX_PACKETS_PER_TICK);
        maxParticlesPerTick = config.getInt(NAME_MAX_PARTICLES_PER_TICK, maxParticlesPerTick, 1, 500, COMMENT_MAX_PARTICLES_PER_TICK);
        maxCasingsPerController = config.getInt(NAME_MAX_CASINGS_PER_CONTROLLER, maxCasingsPerController, 1, 512, COMMENT_MAX_CASINGS_PER_CONTROLLER);
        maxLinesPerProgram = config.getInt(NAME_MAX_LINES_PER_PROGRAM, maxLinesPerProgram, 1, 200, COMMENT_MAX_LINES_PER_PROGRAM);
        maxColumnsPerLine = config.getInt(NAME_MAX_COLUMNS_PER_LINE, maxColumnsPerLine, 1, 80, COMMENT_MAX_COLUMNS_PER_LINE);
        maxInfraredQueueLength = config.getInt(NAME_MAX_QUEUE_LENGTH, maxInfraredQueueLength, 1, 64, COMMENT_MAX_QUEUE_LENGTH);
        animateTypingHand = config.getBoolean(NAME_ANIMATE_TYPING, animateTypingHand, COMMENT_ANIMATE_TYPING);

        // Rebuild list of disabled modules.
        disabledModules.clear();

        final int prefixLength = "module_*".length();
        for (final Identifier module : Constants.MODULES) {
            final String path = module.getPath();
            if (!path.startsWith("module_")) {
                TIS3D.getLog().warn("Module name with bad format, [{}], should start with [module_].", path);
                continue;
            }

            // Strip module and first letter from internal name, lowercase first letter.
            final String name = String.valueOf(path.charAt(prefixLength - 1)).toLowerCase(Locale.ROOT) + path.substring(prefixLength);
            if (!config.getBoolean(String.format(NAME_MODULE_ENABLED_PATTERN, name), true, String.format(COMMENT_MODULE_ENABLED_PATTERN, name))) {
                disabledModules.add(module);
            }
        }

        config.save(configFile);
    }

    // --------------------------------------------------------------------- //

    private Settings() {
    }
}
