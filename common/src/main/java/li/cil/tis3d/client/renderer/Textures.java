package li.cil.tis3d.client.renderer;

import li.cil.tis3d.api.API;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public final class Textures {
    public static final Identifier LOCATION_GUI_BOOK_CODE_BACKGROUND = Identifier.fromNamespaceAndPath(API.MOD_ID, "textures/gui/code_book.png");
    public static final Identifier LOCATION_GUI_MANUAL_BACKGROUND = Identifier.fromNamespaceAndPath(API.MOD_ID, "textures/gui/manual.png");
    public static final Identifier LOCATION_GUI_MANUAL_TAB = Identifier.fromNamespaceAndPath(API.MOD_ID, "textures/gui/manual_tab.png");
    public static final Identifier LOCATION_GUI_MANUAL_SCROLL = Identifier.fromNamespaceAndPath(API.MOD_ID, "textures/gui/manual_scroll.png");
    public static final Identifier LOCATION_GUI_MEMORY = Identifier.fromNamespaceAndPath(API.MOD_ID, "textures/gui/module_memory.png");

    public static final Identifier LOCATION_OVERLAY_CASING_LOCKED = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/overlay/casing_locked");
    public static final Identifier LOCATION_OVERLAY_CASING_UNLOCKED = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/overlay/casing_unlocked");
    public static final Identifier LOCATION_OVERLAY_CASING_PORT_CLOSED = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/overlay/casing_port_closed");
    public static final Identifier LOCATION_OVERLAY_CASING_PORT_OPEN = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/overlay/casing_port_open");
    public static final Identifier LOCATION_OVERLAY_CASING_PORT_HIGHLIGHT = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/overlay/casing_port_highlight");
    public static final Identifier LOCATION_OVERLAY_CASING_PORT_CLOSED_SMALL = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/overlay/casing_port_closed_small");

    public static final Identifier LOCATION_OVERLAY_MODULE_AUDIO = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/overlay/audio_module");
    public static final Identifier LOCATION_OVERLAY_MODULE_EXECUTION_ERROR = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/overlay/execution_module_error");
    public static final Identifier LOCATION_OVERLAY_MODULE_EXECUTION_IDLE = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/overlay/execution_module_idle");
    public static final Identifier LOCATION_OVERLAY_MODULE_EXECUTION_RUNNING = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/overlay/execution_module_running");
    public static final Identifier LOCATION_OVERLAY_MODULE_EXECUTION_WAITING = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/overlay/execution_module_waiting");
    public static final Identifier LOCATION_OVERLAY_MODULE_INFRARED = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/overlay/infrared_module");
    public static final Identifier LOCATION_OVERLAY_MODULE_KEYPAD = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/overlay/keypad_module");
    public static final Identifier LOCATION_OVERLAY_MODULE_QUEUE = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/overlay/queue_module");
    public static final Identifier LOCATION_OVERLAY_MODULE_RANDOM = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/overlay/random_module");
    public static final Identifier LOCATION_OVERLAY_MODULE_REDSTONE = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/overlay/redstone_module");
    public static final Identifier LOCATION_OVERLAY_MODULE_REDSTONE_BARS = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/overlay/redstone_bars_module");
    public static final Identifier LOCATION_OVERLAY_MODULE_SEQUENCER = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/overlay/sequencer_module");
    public static final Identifier LOCATION_OVERLAY_MODULE_SERIAL_PORT = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/overlay/serial_port_module");
    public static final Identifier LOCATION_OVERLAY_MODULE_STACK = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/overlay/stack_module");
    public static final Identifier LOCATION_OVERLAY_MODULE_TERMINAL = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/overlay/terminal_module");
    public static final Identifier LOCATION_OVERLAY_MODULE_TIMER = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/overlay/timer_module");

    // --------------------------------------------------------------------- //

    private Textures() {
    }
}
