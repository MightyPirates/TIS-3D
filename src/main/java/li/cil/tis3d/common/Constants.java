package li.cil.tis3d.common;

import li.cil.tis3d.api.API;

import java.util.regex.Pattern;

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

    public static final String NAME_ITEM_BOOK_CODE = "bookCode";
    public static final String NAME_ITEM_BOOK_MANUAL = "bookManual";
    public static final String NAME_ITEM_KEY = "key";
    public static final String NAME_ITEM_KEY_CREATIVE = "keyCreative";
    public static final String NAME_ITEM_MODULE_AUDIO = "moduleAudio";
    public static final String NAME_ITEM_MODULE_BUNDLED_REDSTONE = "moduleBundledRedstone";
    public static final String NAME_ITEM_MODULE_DISPLAY = "moduleDisplay";
    public static final String NAME_ITEM_MODULE_EXECUTION = "moduleExecution";
    public static final String NAME_ITEM_MODULE_INFRARED = "moduleInfrared";
    public static final String NAME_ITEM_MODULE_KEYPAD = "moduleKeypad";
    public static final String NAME_ITEM_MODULE_QUEUE = "moduleQueue";
    public static final String NAME_ITEM_MODULE_RANDOM = "moduleRandom";
    public static final String NAME_ITEM_MODULE_RANDOM_ACCESS_MEMORY = "moduleRandomAccessMemory";
    public static final String NAME_ITEM_MODULE_READ_ONLY_MEMORY = "moduleReadOnlyMemory";
    public static final String NAME_ITEM_MODULE_REDSTONE = "moduleRedstone";
    public static final String NAME_ITEM_MODULE_SERIAL_PORT = "moduleSerialPort";
    public static final String NAME_ITEM_MODULE_STACK = "moduleStack";
    public static final String NAME_ITEM_MODULE_TERMINAL = "moduleTerminal";
    public static final String NAME_ITEM_MODULE_TIMER = "moduleTimer";
    public static final String NAME_ITEM_PRISM = "prism";

    public static final String NAME_ENTITY_INFRARED_PACKET = API.MOD_ID + ":infraredPacket";

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
    public static final String MESSAGE_INVALID_FORMAT = "tis3d.compiler.invalidFormat";
    public static final String MESSAGE_INVALID_INSTRUCTION = "tis3d.compiler.invalidInstruction";
    public static final String MESSAGE_LABEL_DUPLICATE = "tis3d.compiler.labelDuplicate";
    public static final String MESSAGE_LABEL_NOT_FOUND = "tis3d.compiler.labelNotFound";
    public static final String MESSAGE_PARAMETER_INVALID = "tis3d.compiler.parameterInvalid";
    public static final String MESSAGE_PARAMETER_OVERFLOW = "tis3d.compiler.parameterOverflow";
    public static final String MESSAGE_PARAMETER_UNDERFLOW = "tis3d.compiler.parameterUnderflow";
    public static final String MESSAGE_TOO_MANY_COLUMNS = "tis3d.compiler.tooManyColumns";
    public static final String MESSAGE_TOO_MANY_LINES = "tis3d.compiler.tooManyLines";

    // --------------------------------------------------------------------- //
    // Code book

    public static final int MAX_LINES_PER_PAGE = 20;
    public static final Pattern PATTERN_LINES = Pattern.compile("\r?\n");
    public static final String MESSAGE_ERROR_ON_PREVIOUS_PAGE = "tis3d.book_code.error_on_previous_page";
    public static final String MESSAGE_ERROR_ON_NEXT_PAGE = "tis3d.book_code.error_on_next_page";

    // --------------------------------------------------------------------- //
    // Tooltips
    public static final int MAX_TOOLTIP_WIDTH = 200;

    public static final String TOOLTIP_BOOK_CODE = "tis3d.tooltip.bookCode";
    public static final String TOOLTIP_BOOK_MANUAL = "tis3d.tooltip.bookManual";
    public static final String TOOLTIP_KEY = "tis3d.tooltip.key";

    // --------------------------------------------------------------------- //

    private Constants() {
    }
}
