package li.cil.tis3d.client.init;

import li.cil.tis3d.api.API;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.util.Identifier;

public final class Textures {
    public static final Identifier LOCATION_GUI_BOOK_CODE_BACKGROUND = new Identifier(API.MOD_ID, "textures/gui/book_code.png");
    public static final Identifier LOCATION_GUI_MANUAL_BACKGROUND = new Identifier(API.MOD_ID, "textures/gui/manual.png");
    public static final Identifier LOCATION_GUI_MANUAL_TAB = new Identifier(API.MOD_ID, "textures/gui/manual_tab.png");
    public static final Identifier LOCATION_GUI_MANUAL_SCROLL = new Identifier(API.MOD_ID, "textures/gui/manual_scroll.png");
    public static final Identifier LOCATION_GUI_MANUAL_MISSING = new Identifier(API.MOD_ID, "textures/gui/manual_missing.png");
    public static final Identifier LOCATION_GUI_MEMORY = new Identifier(API.MOD_ID, "textures/gui/module_memory.png");

    public static final Identifier LOCATION_OVERLAY_UTIL_WHITE = new Identifier(API.MOD_ID, "block/overlay/util_white");
    public static final Identifier LOCATION_OVERLAY_CASING_LOCKED = new Identifier(API.MOD_ID, "block/overlay/casing_locked");
    public static final Identifier LOCATION_OVERLAY_CASING_UNLOCKED = new Identifier(API.MOD_ID, "block/overlay/casing_unlocked");
    public static final Identifier LOCATION_OVERLAY_CASING_PORT_CLOSED = new Identifier(API.MOD_ID, "block/overlay/casing_port_closed");
    public static final Identifier LOCATION_OVERLAY_CASING_PORT_OPEN = new Identifier(API.MOD_ID, "block/overlay/casing_port_open");
    public static final Identifier LOCATION_OVERLAY_CASING_PORT_HIGHLIGHT = new Identifier(API.MOD_ID, "block/overlay/casing_port_highlight");
    public static final Identifier LOCATION_OVERLAY_CASING_PORT_CLOSED_SMALL = new Identifier(API.MOD_ID, "block/overlay/casing_port_closed_small");
    public static final Identifier LOCATION_OVERLAY_MODULE_AUDIO = new Identifier(API.MOD_ID, "block/overlay/module_audio");
    public static final Identifier LOCATION_OVERLAY_MODULE_BUNDLED_REDSTONE = new Identifier(API.MOD_ID, "block/overlay/module_bundled_redstone");
    public static final Identifier LOCATION_OVERLAY_MODULE_EXECUTION_ERROR = new Identifier(API.MOD_ID, "block/overlay/module_execution_error");
    public static final Identifier LOCATION_OVERLAY_MODULE_EXECUTION_IDLE = new Identifier(API.MOD_ID, "block/overlay/module_execution_idle");
    public static final Identifier LOCATION_OVERLAY_MODULE_EXECUTION_RUNNING = new Identifier(API.MOD_ID, "block/overlay/module_execution_running");
    public static final Identifier LOCATION_OVERLAY_MODULE_EXECUTION_WAITING = new Identifier(API.MOD_ID, "block/overlay/module_execution_waiting");
    public static final Identifier LOCATION_OVERLAY_MODULE_INFRARED = new Identifier(API.MOD_ID, "block/overlay/module_infrared");
    public static final Identifier LOCATION_OVERLAY_MODULE_KEYPAD = new Identifier(API.MOD_ID, "block/overlay/module_keypad");
    public static final Identifier LOCATION_OVERLAY_MODULE_QUEUE = new Identifier(API.MOD_ID, "block/overlay/module_queue");
    public static final Identifier LOCATION_OVERLAY_MODULE_RANDOM = new Identifier(API.MOD_ID, "block/overlay/module_random");
    public static final Identifier LOCATION_OVERLAY_MODULE_REDSTONE = new Identifier(API.MOD_ID, "block/overlay/module_redstone");
    public static final Identifier LOCATION_OVERLAY_MODULE_REDSTONE_BARS = new Identifier(API.MOD_ID, "block/overlay/module_redstone_bars");
    public static final Identifier LOCATION_OVERLAY_MODULE_SEQUENCER = new Identifier(API.MOD_ID, "block/overlay/module_sequencer");
    public static final Identifier LOCATION_OVERLAY_MODULE_SERIAL_PORT = new Identifier(API.MOD_ID, "block/overlay/module_serial_port");
    public static final Identifier LOCATION_OVERLAY_MODULE_STACK = new Identifier(API.MOD_ID, "block/overlay/module_stack");
    public static final Identifier LOCATION_OVERLAY_MODULE_TERMINAL = new Identifier(API.MOD_ID, "block/overlay/module_terminal");
    public static final Identifier LOCATION_OVERLAY_MODULE_TIMER = new Identifier(API.MOD_ID, "block/overlay/module_timer");

    static void registerSprites(final ClientSpriteRegistryCallback.Registry registry) {
        registry.register(LOCATION_OVERLAY_UTIL_WHITE);
        registry.register(LOCATION_OVERLAY_CASING_LOCKED);
        registry.register(LOCATION_OVERLAY_CASING_UNLOCKED);
        registry.register(LOCATION_OVERLAY_CASING_PORT_CLOSED);
        registry.register(LOCATION_OVERLAY_CASING_PORT_OPEN);
        registry.register(LOCATION_OVERLAY_CASING_PORT_HIGHLIGHT);
        registry.register(LOCATION_OVERLAY_CASING_PORT_CLOSED_SMALL);
        registry.register(LOCATION_OVERLAY_MODULE_AUDIO);
        registry.register(LOCATION_OVERLAY_MODULE_BUNDLED_REDSTONE);
        registry.register(LOCATION_OVERLAY_MODULE_EXECUTION_ERROR);
        registry.register(LOCATION_OVERLAY_MODULE_EXECUTION_IDLE);
        registry.register(LOCATION_OVERLAY_MODULE_EXECUTION_RUNNING);
        registry.register(LOCATION_OVERLAY_MODULE_EXECUTION_WAITING);
        registry.register(LOCATION_OVERLAY_MODULE_INFRARED);
        registry.register(LOCATION_OVERLAY_MODULE_KEYPAD);
        registry.register(LOCATION_OVERLAY_MODULE_QUEUE);
        registry.register(LOCATION_OVERLAY_MODULE_RANDOM);
        registry.register(LOCATION_OVERLAY_MODULE_REDSTONE);
        registry.register(LOCATION_OVERLAY_MODULE_REDSTONE_BARS);
        registry.register(LOCATION_OVERLAY_MODULE_SEQUENCER);
        registry.register(LOCATION_OVERLAY_MODULE_SERIAL_PORT);
        registry.register(LOCATION_OVERLAY_MODULE_STACK);
        registry.register(LOCATION_OVERLAY_MODULE_TERMINAL);
        registry.register(LOCATION_OVERLAY_MODULE_TIMER);
    }

    // --------------------------------------------------------------------- //

    public Textures() {
    }
}
