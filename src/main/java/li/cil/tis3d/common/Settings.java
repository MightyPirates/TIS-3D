package li.cil.tis3d.common;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import net.fabricmc.loader.FabricLoader;
import pl.asie.protocharset.lib.repack.blue.endless.jankson.Jankson;
import pl.asie.protocharset.lib.repack.blue.endless.jankson.JsonObject;
import pl.asie.protocharset.lib.repack.blue.endless.jankson.JsonPrimitive;
import pl.asie.protocharset.lib.repack.blue.endless.jankson.impl.SyntaxError;
import net.minecraft.util.Identifier;
import java.io.File;
import java.io.IOException;
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
    public static int maxColumnsPerLine = 18;

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
    public static final Set<String> disabledModules = new HashSet<>();

    // --------------------------------------------------------------------- //

    private static final String CONFIG_VERSION = "1";

    private static final String CATEGORY_NETWORK = "network.";
    private static final String CATEGORY_CONTROLLER = "controller.";
    private static final String CATEGORY_MODULE = "module.";
    private static final String CATEGORY_MODULE_EXECUTION = "module.execution.";
    private static final String CATEGORY_MODULE_INFRARED = "module.infrared.";
    private static final String CATEGORY_MODULE_TERMINAL = "module.terminal.";

    private static final String NAME_ANIMATE_TYPING = "animateTyping";
    private static final String NAME_MAX_PACKETS_PER_TICK = "maxPacketsPerTick";
    private static final String NAME_MAX_PARTICLES_PER_TICK = "maxParticlesPerTick";
    private static final String NAME_MAX_CASINGS_PER_CONTROLLER = "maxCasings";
    private static final String NAME_MAX_LINES_PER_PROGRAM = "maxLinesPerProgram";
    private static final String NAME_MAX_COLUMNS_PER_LINE = "maxColumnsPerLine";
    private static final String NAME_MAX_QUEUE_LENGTH = "maxQueueLength";
    private static final String NAME_MODULE_ENABLED = "enabled";

    private static final String COMMENT_ANIMATE_TYPING = "Whether to swing the player's arm while typing on a terminal module.";
    private static final String COMMENT_MAX_PACKETS_PER_TICK = "The maximum number of status packets modules may send per tick. When this is exceeded, throttling kicks in.";
    private static final String COMMENT_MAX_PARTICLES_PER_TICK = "The maximum number of particle effects data transfer may trigger per tick. When this is exceeded, throttling kicks in.";
    private static final String COMMENT_MAX_CASINGS_PER_CONTROLLER = "The maximum number of casings a single controller supports.";
    private static final String COMMENT_MAX_LINES_PER_PROGRAM = "The maximum number of lines an ASM program for an execution node may have.";
    private static final String COMMENT_MAX_COLUMNS_PER_LINE = "The maximum number of columns per line of an ASM program for an execution node may have.";
    private static final String COMMENT_MAX_QUEUE_LENGTH = "The maximum number of infrared packets that can be stored in the receiver's buffer.";
    private static final String COMMENT_MODULE_ENABLED = "Whether the module is enabled. Disabled modules are not registered, meaning if you disable them later on the items will disappear!";

    // --------------------------------------------------------------------- //

    private static boolean loaded;

    @SuppressWarnings("ConstantConditions")
    public static void load() {
        if (loaded) return;
        loaded = true;
        File configDir = FabricLoader.INSTANCE.getConfigDirectory();
        if (!configDir.exists()) configDir.mkdir();
        File configFile = new File(configDir, "tis3d.hjson");

        JsonObject config;
        try {
            config = Jankson.builder().build().load(configFile);
        } catch (SyntaxError | IOException var9) {
            config = new JsonObject();
        }

        config.putDefault("network", new JsonObject(), null);
        config.putDefault("controller", new JsonObject(), null);
        config.putDefault("module", new JsonObject(), null);

        // Rebuild list of disabled modules.
        disabledModules.clear();

        for (final Identifier module : Constants.MODULES) {
            final String name;
            if (module.getPath().startsWith("module_")) {
                name = module.getPath().substring(7, 8).toLowerCase(Locale.ROOT) + module.getPath().substring(8);
            } else {
                name = module.getPath().substring(0, 1).toLowerCase(Locale.ROOT) + module.getPath().substring(1);
            }
            config.recursiveGet(JsonObject.class, "module").putDefault(name, new JsonObject(), null);
            config.recursiveGet(JsonObject.class, CATEGORY_MODULE + name).putDefault("enabled", new JsonPrimitive(true), COMMENT_MODULE_ENABLED);

            boolean enabled = config.recursiveGet(JsonObject.class, CATEGORY_MODULE + name).get(Boolean.class, "enabled");
            if (!enabled) {
                disabledModules.add(name);
            }
        }

        config.recursiveGet(JsonObject.class, "network").putDefault(NAME_MAX_PACKETS_PER_TICK, new JsonPrimitive(maxPacketsPerTick), COMMENT_MAX_PACKETS_PER_TICK + " [1-500]");
        config.recursiveGet(JsonObject.class, "network").putDefault(NAME_MAX_PARTICLES_PER_TICK, new JsonPrimitive(maxParticlesPerTick), COMMENT_MAX_PARTICLES_PER_TICK + " [1-500]");
        config.recursiveGet(JsonObject.class, "controller").putDefault(NAME_MAX_CASINGS_PER_CONTROLLER, new JsonPrimitive(maxCasingsPerController), COMMENT_MAX_CASINGS_PER_CONTROLLER + " [1-512]");
        config.recursiveGet(JsonObject.class, "module.execution").putDefault(NAME_MAX_LINES_PER_PROGRAM, new JsonPrimitive(maxLinesPerProgram), COMMENT_MAX_LINES_PER_PROGRAM + " [1-200]");
        config.recursiveGet(JsonObject.class, "module.execution").putDefault(NAME_MAX_COLUMNS_PER_LINE, new JsonPrimitive(maxColumnsPerLine), COMMENT_MAX_COLUMNS_PER_LINE + " [1-80]");
        config.recursiveGet(JsonObject.class, "module.infrared").putDefault(NAME_MAX_QUEUE_LENGTH, new JsonPrimitive(maxInfraredQueueLength), COMMENT_MAX_QUEUE_LENGTH + " [1-64]");
        config.recursiveGet(JsonObject.class, "module.terminal").putDefault(NAME_ANIMATE_TYPING, new JsonPrimitive(animateTypingHand), COMMENT_ANIMATE_TYPING);

        maxPacketsPerTick = config.recursiveGet(Integer.TYPE, CATEGORY_NETWORK + NAME_MAX_PACKETS_PER_TICK);
        maxParticlesPerTick = config.recursiveGet(Integer.TYPE, CATEGORY_NETWORK + NAME_MAX_PARTICLES_PER_TICK);
        maxCasingsPerController = config.recursiveGet(Integer.TYPE, CATEGORY_CONTROLLER + NAME_MAX_CASINGS_PER_CONTROLLER);
        maxLinesPerProgram = config.recursiveGet(Integer.TYPE, CATEGORY_MODULE_EXECUTION + NAME_MAX_LINES_PER_PROGRAM);
        maxColumnsPerLine = config.recursiveGet(Integer.TYPE, CATEGORY_MODULE_EXECUTION + NAME_MAX_COLUMNS_PER_LINE);
        maxInfraredQueueLength = config.recursiveGet(Integer.TYPE, CATEGORY_MODULE_INFRARED + NAME_MAX_QUEUE_LENGTH);
        animateTypingHand = config.recursiveGet(Boolean.TYPE, CATEGORY_MODULE_TERMINAL + NAME_ANIMATE_TYPING);

        try {
            Files.write(config.toJson(true, true), configFile, Charsets.UTF_8);
        } catch (Exception var8) {
            var8.printStackTrace();
        }

        // TODO
        /* final Configuration config = new Configuration(configFile, CONFIG_VERSION);

        config.load();

        upgradeConfig(config);

        maxPacketsPerTick = config.getInt(NAME_MAX_PACKETS_PER_TICK, CATEGORY_NETWORK,
            maxPacketsPerTick, 1, 500, COMMENT_MAX_PACKETS_PER_TICK);
        maxParticlesPerTick = config.getInt(NAME_MAX_PARTICLES_PER_TICK, CATEGORY_NETWORK,
            maxParticlesPerTick, 1, 500, COMMENT_MAX_PARTICLES_PER_TICK);

        maxCasingsPerController = config.getInt(NAME_MAX_CASINGS_PER_CONTROLLER, CATEGORY_CONTROLLER,
            maxCasingsPerController, 1, 512, COMMENT_MAX_CASINGS_PER_CONTROLLER);

        maxLinesPerProgram = config.getInt(NAME_MAX_LINES_PER_PROGRAM, CATEGORY_MODULE_EXECUTION,
            maxLinesPerProgram, 1, 200, COMMENT_MAX_LINES_PER_PROGRAM);
        maxColumnsPerLine = config.getInt(NAME_MAX_COLUMNS_PER_LINE, CATEGORY_MODULE_EXECUTION,
            maxColumnsPerLine, 1, 80, COMMENT_MAX_COLUMNS_PER_LINE);
        maxInfraredQueueLength = config.getInt(NAME_MAX_QUEUE_LENGTH, CATEGORY_MODULE_INFRARED,
            maxInfraredQueueLength, 1, 64, COMMENT_MAX_QUEUE_LENGTH);
        animateTypingHand = config.getBoolean(NAME_ANIMATE_TYPING, CATEGORY_MODULE_TERMINAL,
            animateTypingHand, COMMENT_ANIMATE_TYPING);

        // Rebuild list of disabled modules.
        disabledModules.clear();
        // Strip module and first letter from internal name, lowercase first letter.
        final int prefixLength = "module_*".length();
        for (final String module : Constants.MODULES) {
            final String name = String.valueOf(module.charAt(prefixLength - 1)).toLowerCase(Locale.US) + module.substring(prefixLength);
            checkModule(config, CATEGORY_MODULE + "." + name, module);
        }

        if (config.hasChanged()) {
            config.save();
        } */
    }

/*    private static void upgradeConfig(final Configuration config) {
        final String loadedVersion = config.getLoadedConfigVersion();
        int loadedVersionInt;
        try {
            loadedVersionInt = Integer.parseInt(loadedVersion);
        } catch (final NumberFormatException e) {
            loadedVersionInt = 0;
        }

        // Incremental upgrade logic: fall through starting at old version.
        switch (loadedVersionInt) {
            case 0:
                config.get(CATEGORY_MODULE_EXECUTION, NAME_MAX_LINES_PER_PROGRAM, maxLinesPerProgram,
                    COMMENT_MAX_LINES_PER_PROGRAM, 1, 200).set(40);

                for (final String module : Constants.MODULES) {
                    final String moduleName = module.substring("module_".length());
                    final String oldModuleName = moduleName.replace("_", "");
                    config.removeCategory(config.getCategory(CATEGORY_MODULE + "._" + moduleName));
                    config.removeCategory(config.getCategory(CATEGORY_MODULE + "." + oldModuleName));
                }
            default:
                break;
        }
    }

    private static void checkModule(final Configuration config, final String path, final String name) {
        if (!config.getBoolean(NAME_MODULE_ENABLED, path, true, COMMENT_MODULE_ENABLED)) {
            disabledModules.add(name);
        }
    } */

    // --------------------------------------------------------------------- //

    private Settings() {
    }
}
