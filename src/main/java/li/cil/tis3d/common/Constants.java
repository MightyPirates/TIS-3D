package li.cil.tis3d.common;

import li.cil.tis3d.api.API;

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
    public static final String NAME_ITEM_MODULE_AUDIO = "moduleAudio";
    public static final String NAME_ITEM_MODULE_BUNDLED_REDSTONE = "moduleBundledRedstone";
    public static final String NAME_ITEM_MODULE_DISPLAY = "moduleDisplay";
    public static final String NAME_ITEM_MODULE_EXECUTION = "moduleExecution";
    public static final String NAME_ITEM_MODULE_INFRARED = "moduleInfrared";
    public static final String NAME_ITEM_MODULE_REDSTONE = "moduleRedstone";
    public static final String NAME_ITEM_MODULE_STACK = "moduleStack";
    public static final String NAME_ITEM_MODULE_RANDOM = "moduleRandom";
    public static final String NAME_ITEM_PRISM = "prism";

    public static final String NAME_ENTITY_INFRARED_PACKET = API.MOD_ID + ":infraredPacket";

    public static final String NAME_INVENTORY_CASING = "container.casing";

    // --------------------------------------------------------------------- //
    // Compiler errors

    public static final String MESSAGE_TOO_MANY_LINES = "tis3d.compiler.tooManyLines";
    public static final String MESSAGE_LINE_TOO_LONG = "tis3d.compiler.tooManyColumns";
    public static final String MESSAGE_UNEXPECTED_TOKEN = "tis3d.compiler.invalidFormat";
    public static final String MESSAGE_UNKNOWN_INSTRUCTION = "tis3d.compiler.invalidInstruction";
    public static final String MESSAGE_MISSING_PARAMETER = "tis3d.compiler.parameterUnderflow";
    public static final String MESSAGE_INVALID_TARGET = "tis3d.compiler.parameterInvalid";
    public static final String MESSAGE_EXCESS_TOKENS = "tis3d.compiler.parameterOverflow";
    public static final String MESSAGE_NO_SUCH_LABEL = "tis3d.compiler.labelNotFound";
    public static final String MESSAGE_DUPLICATE_LABEL = "tis3d.compiler.labelDuplicate";

    // --------------------------------------------------------------------- //

    private Constants() {
    }
}
