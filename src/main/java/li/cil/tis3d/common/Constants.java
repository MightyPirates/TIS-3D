package li.cil.tis3d.common;

/**
 * Collection of constants used throughout the mod.
 */
public final class Constants {
    // --------------------------------------------------------------------- //
    // Mod data

    public static final String PROXY_CLIENT = "li.cil.tis3d.client.ProxyClient";
    public static final String PROXY_COMMON = "li.cil.tis3d.common.ProxyCommon";

    // --------------------------------------------------------------------- //
    // Block, item, entity and container names

    public static final String NAME_BLOCK_CASING = "casing";
    public static final String NAME_BLOCK_CONTROLLER = "controller";

    public static final String NAME_ITEM_BOOK_CODE = "book_code";
    public static final String NAME_ITEM_BOOK_MANUAL = "book_manual";
    public static final String NAME_ITEM_KEY = "key";
    public static final String NAME_ITEM_KEY_CREATIVE = "key_creative";
    public static final String NAME_ITEM_MODULE_AUDIO = "module_audio";
    public static final String NAME_ITEM_MODULE_BUNDLED_REDSTONE = "module_bundled_redstone";
    public static final String NAME_ITEM_MODULE_DISPLAY = "module_display";
    public static final String NAME_ITEM_MODULE_EXECUTION = "module_execution";
    public static final String NAME_ITEM_MODULE_INFRARED = "module_infrared";
    public static final String NAME_ITEM_MODULE_KEYPAD = "module_keypad";
    public static final String NAME_ITEM_MODULE_REDSTONE = "module_redstone";
    public static final String NAME_ITEM_MODULE_SERIAL_PORT = "module_serial_port";
    public static final String NAME_ITEM_MODULE_STACK = "module_stack";
    public static final String NAME_ITEM_MODULE_RANDOM = "module_random";
    public static final String NAME_ITEM_MODULE_RANDOM_ACCESS_MEMORY = "module_random_access_memory";
    public static final String NAME_ITEM_PRISM = "prism";

    public static final String NAME_ENTITY_INFRARED_PACKET = "infrared_packet";

    public static final String NAME_INVENTORY_CASING = "container.casing";

    public static final String[] MODULES = new String[]{
            NAME_ITEM_MODULE_AUDIO,
            NAME_ITEM_MODULE_BUNDLED_REDSTONE,
            NAME_ITEM_MODULE_DISPLAY,
            NAME_ITEM_MODULE_EXECUTION,
            NAME_ITEM_MODULE_INFRARED,
            NAME_ITEM_MODULE_KEYPAD,
            NAME_ITEM_MODULE_REDSTONE,
            NAME_ITEM_MODULE_SERIAL_PORT,
            NAME_ITEM_MODULE_STACK,
            NAME_ITEM_MODULE_RANDOM,
            NAME_ITEM_MODULE_RANDOM_ACCESS_MEMORY
    };

    // --------------------------------------------------------------------- //
    // Compiler errors

    public static final String MESSAGE_TOO_MANY_LINES = "tis3d.compiler.too_many_lines";
    public static final String MESSAGE_LINE_TOO_LONG = "tis3d.compiler.too_many_columns";
    public static final String MESSAGE_UNEXPECTED_TOKEN = "tis3d.compiler.invalid_format";
    public static final String MESSAGE_UNKNOWN_INSTRUCTION = "tis3d.compiler.invalid_instruction";
    public static final String MESSAGE_MISSING_PARAMETER = "tis3d.compiler.parameter_underflow";
    public static final String MESSAGE_INVALID_TARGET = "tis3d.compiler.parameter_invalid";
    public static final String MESSAGE_EXCESS_TOKENS = "tis3d.compiler.parameter_overflow";
    public static final String MESSAGE_NO_SUCH_LABEL = "tis3d.compiler.label_not_found";
    public static final String MESSAGE_DUPLICATE_LABEL = "tis3d.compiler.label_duplicate";

    // --------------------------------------------------------------------- //

    public static final int MAX_TOOLTIP_WIDTH = 200;

    // --------------------------------------------------------------------- //

    private Constants() {
    }
}
