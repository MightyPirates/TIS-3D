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
    public static final String NAME_ITEM_CODE_BOOK = "codeBook";
    public static final String NAME_ITEM_MODULE_EXECUTION = "moduleExecution";
    public static final String NAME_ITEM_MODULE_INFRARED = "moduleInfrared";
    public static final String NAME_ITEM_MODULE_REDSTONE = "moduleRedstone";
    public static final String NAME_ITEM_MODULE_STACK = "moduleStack";
    public static final String NAME_ITEM_MODULE_RANDOM = "moduleRandom";

    public static final String NAME_ENTITY_INFRARED_PACKET = API.MOD_ID + ":infraredPacket";

    public static final String NAME_INVENTORY_CASING = "container.casing";

    // --------------------------------------------------------------------- //
    // Compiler errors

    public static final String MESSAGE_TOO_MANY_LINES = "Too many lines";
    public static final String MESSAGE_LINE_TOO_LONG = "Line too long";
    public static final String MESSAGE_UNEXPECTED_TOKEN = "Unexpected token";
    public static final String MESSAGE_UNKNOWN_INSTRUCTION = "Invalid opcode";
    public static final String MESSAGE_MISSING_PARAMETER = "Missing operand";
    public static final String MESSAGE_INVALID_TARGET = "Invalid register";
    public static final String MESSAGE_EXCESS_TOKENS = "Too many operands";
    public static final String MESSAGE_NO_SUCH_LABEL = "Undefined label";

    // --------------------------------------------------------------------- //

    private Constants() {
    }
}
