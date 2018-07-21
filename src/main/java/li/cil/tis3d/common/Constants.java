package li.cil.tis3d.common;

import java.util.regex.Pattern;

/**
 * Collection of constants used throughout the mod.
 */
public final class Constants {
    // --------------------------------------------------------------------- //
    // Mod data

    static final String PROXY_CLIENT = "li.cil.tis3d.client.ProxyClient";
    static final String PROXY_COMMON = "li.cil.tis3d.common.ProxyCommon";

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
    public static final String NAME_ITEM_MODULE_QUEUE = "module_queue";
    public static final String NAME_ITEM_MODULE_RANDOM = "module_random";
    public static final String NAME_ITEM_MODULE_RANDOM_ACCESS_MEMORY = "module_random_access_memory";
    public static final String NAME_ITEM_MODULE_READ_ONLY_MEMORY = "module_read_only_memory";
    public static final String NAME_ITEM_MODULE_REDSTONE = "module_redstone";
    public static final String NAME_ITEM_MODULE_SERIAL_PORT = "module_serial_port";
    public static final String NAME_ITEM_MODULE_STACK = "module_stack";
    public static final String NAME_ITEM_MODULE_TERMINAL = "module_terminal";
    public static final String NAME_ITEM_MODULE_TIMER = "module_timer";
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
            NAME_ITEM_MODULE_QUEUE,
            NAME_ITEM_MODULE_RANDOM,
            NAME_ITEM_MODULE_RANDOM_ACCESS_MEMORY,
            NAME_ITEM_MODULE_READ_ONLY_MEMORY,
            NAME_ITEM_MODULE_REDSTONE,
            NAME_ITEM_MODULE_SERIAL_PORT,
            NAME_ITEM_MODULE_STACK,
            NAME_ITEM_MODULE_TERMINAL,
            NAME_ITEM_MODULE_TIMER
    };

    // --------------------------------------------------------------------- //
    // Compiler errors

    public static final String MESSAGE_COMPILE_ERROR = "tis3d.compiler.error";
    public static final String MESSAGE_INVALID_FORMAT = "tis3d.compiler.invalid_format";
    public static final String MESSAGE_INVALID_INSTRUCTION = "tis3d.compiler.invalid_instruction";
    public static final String MESSAGE_LABEL_DUPLICATE = "tis3d.compiler.label_duplicate";
    public static final String MESSAGE_LABEL_NOT_FOUND = "tis3d.compiler.label_not_found";
    public static final String MESSAGE_PARAMETER_INVALID = "tis3d.compiler.parameter_invalid";
    public static final String MESSAGE_PARAMETER_OVERFLOW = "tis3d.compiler.parameter_overflow";
    public static final String MESSAGE_PARAMETER_UNDERFLOW = "tis3d.compiler.parameter_underflow";
    public static final String MESSAGE_TOO_MANY_COLUMNS = "tis3d.compiler.too_many_columns";
    public static final String MESSAGE_TOO_MANY_LINES = "tis3d.compiler.too_many_lines";

    // --------------------------------------------------------------------- //
    // Code book

    public static final int MAX_LINES_PER_PAGE = 20;
    public static final Pattern PATTERN_LINES = Pattern.compile("\r?\n");
    public static final String MESSAGE_ERROR_ON_PREVIOUS_PAGE = "tis3d.book_code.error_on_previous_page";
    public static final String MESSAGE_ERROR_ON_NEXT_PAGE = "tis3d.book_code.error_on_next_page";

    // --------------------------------------------------------------------- //
    // Tooltips

    public static final int MAX_TOOLTIP_WIDTH = 200;

    public static final String TOOLTIP_BOOK_CODE = "tis3d.tooltip.book_code";
    public static final String TOOLTIP_BOOK_MANUAL = "tis3d.tooltip.book_manual";
    public static final String TOOLTIP_KEY = "tis3d.tooltip.key";

    // --------------------------------------------------------------------- //

    private Constants() {
    }
}
