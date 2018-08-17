package li.cil.tis3d.client.renderer;

import li.cil.tis3d.api.API;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import pl.asie.protocharset.rift.listeners.client.TextureLocationLoader;

public final class TextureLoader implements TextureLocationLoader {
    public static final ResourceLocation LOCATION_GUI_BOOK_CODE_BACKGROUND = new ResourceLocation(API.MOD_ID, "textures/gui/book_code.png");
    public static final ResourceLocation LOCATION_GUI_MANUAL_BACKGROUND = new ResourceLocation(API.MOD_ID, "textures/gui/manual.png");
    public static final ResourceLocation LOCATION_GUI_MANUAL_TAB = new ResourceLocation(API.MOD_ID, "textures/gui/manual_tab.png");
    public static final ResourceLocation LOCATION_GUI_MANUAL_SCROLL = new ResourceLocation(API.MOD_ID, "textures/gui/manual_scroll.png");
    public static final ResourceLocation LOCATION_GUI_MANUAL_MISSING = new ResourceLocation(API.MOD_ID, "textures/gui/manual_missing.png");
    public static final ResourceLocation LOCATION_GUI_MEMORY = new ResourceLocation(API.MOD_ID, "textures/gui/module_memory.png");

    public static final ResourceLocation LOCATION_OVERLAY_CASING_LOCKED = new ResourceLocation(API.MOD_ID, "block/overlay/casing_locked");
    public static final ResourceLocation LOCATION_OVERLAY_CASING_UNLOCKED = new ResourceLocation(API.MOD_ID, "block/overlay/casing_unlocked");
    public static final ResourceLocation LOCATION_OVERLAY_CASING_PORT_CLOSED = new ResourceLocation(API.MOD_ID, "block/overlay/casing_port_closed");
    public static final ResourceLocation LOCATION_OVERLAY_CASING_PORT_OPEN = new ResourceLocation(API.MOD_ID, "block/overlay/casing_port_open");
    public static final ResourceLocation LOCATION_OVERLAY_CASING_PORT_HIGHLIGHT = new ResourceLocation(API.MOD_ID, "block/overlay/casing_port_highlight");
    public static final ResourceLocation LOCATION_OVERLAY_CASING_PORT_CLOSED_SMALL = new ResourceLocation(API.MOD_ID, "block/overlay/casing_port_closed_small");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_AUDIO = new ResourceLocation(API.MOD_ID, "block/overlay/module_audio");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_BUNDLED_REDSTONE = new ResourceLocation(API.MOD_ID, "block/overlay/module_bundled_redstone");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_EXECUTION_ERROR = new ResourceLocation(API.MOD_ID, "block/overlay/module_execution_error");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_EXECUTION_IDLE = new ResourceLocation(API.MOD_ID, "block/overlay/module_execution_idle");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_EXECUTION_RUNNING = new ResourceLocation(API.MOD_ID, "block/overlay/module_execution_running");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_EXECUTION_WAITING = new ResourceLocation(API.MOD_ID, "block/overlay/module_execution_waiting");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_INFRARED = new ResourceLocation(API.MOD_ID, "block/overlay/module_infrared");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_KEYPAD = new ResourceLocation(API.MOD_ID, "block/overlay/module_keypad");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_QUEUE = new ResourceLocation(API.MOD_ID, "block/overlay/module_queue");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_RANDOM = new ResourceLocation(API.MOD_ID, "block/overlay/module_random");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_REDSTONE = new ResourceLocation(API.MOD_ID, "block/overlay/module_redstone");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_REDSTONE_BARS = new ResourceLocation(API.MOD_ID, "block/overlay/module_redstone_bars");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_SEQUENCER = new ResourceLocation(API.MOD_ID, "block/overlay/module_sequencer");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_SERIAL_PORT = new ResourceLocation(API.MOD_ID, "block/overlay/module_serial_port");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_STACK = new ResourceLocation(API.MOD_ID, "block/overlay/module_stack");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_TERMINAL = new ResourceLocation(API.MOD_ID, "block/overlay/module_terminal");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_TIMER = new ResourceLocation(API.MOD_ID, "block/overlay/module_timer");

    public static final TextureLoader INSTANCE = new TextureLoader();

    @Override
    public void loadTextureLocations(IResourceManager manager, TextureMap map) {
        map.registerSprite(manager, LOCATION_OVERLAY_CASING_LOCKED);
        map.registerSprite(manager, LOCATION_OVERLAY_CASING_UNLOCKED);
        map.registerSprite(manager, LOCATION_OVERLAY_CASING_PORT_CLOSED);
        map.registerSprite(manager, LOCATION_OVERLAY_CASING_PORT_OPEN);
        map.registerSprite(manager, LOCATION_OVERLAY_CASING_PORT_HIGHLIGHT);
        map.registerSprite(manager, LOCATION_OVERLAY_CASING_PORT_CLOSED_SMALL);
        map.registerSprite(manager, LOCATION_OVERLAY_MODULE_AUDIO);
        map.registerSprite(manager, LOCATION_OVERLAY_MODULE_BUNDLED_REDSTONE);
        map.registerSprite(manager, LOCATION_OVERLAY_MODULE_EXECUTION_ERROR);
        map.registerSprite(manager, LOCATION_OVERLAY_MODULE_EXECUTION_IDLE);
        map.registerSprite(manager, LOCATION_OVERLAY_MODULE_EXECUTION_RUNNING);
        map.registerSprite(manager, LOCATION_OVERLAY_MODULE_EXECUTION_WAITING);
        map.registerSprite(manager, LOCATION_OVERLAY_MODULE_INFRARED);
        map.registerSprite(manager, LOCATION_OVERLAY_MODULE_KEYPAD);
        map.registerSprite(manager, LOCATION_OVERLAY_MODULE_QUEUE);
        map.registerSprite(manager, LOCATION_OVERLAY_MODULE_RANDOM);
        map.registerSprite(manager, LOCATION_OVERLAY_MODULE_REDSTONE);
        map.registerSprite(manager, LOCATION_OVERLAY_MODULE_REDSTONE_BARS);
        map.registerSprite(manager, LOCATION_OVERLAY_MODULE_SEQUENCER);
        map.registerSprite(manager, LOCATION_OVERLAY_MODULE_SERIAL_PORT);
        map.registerSprite(manager, LOCATION_OVERLAY_MODULE_STACK);
        map.registerSprite(manager, LOCATION_OVERLAY_MODULE_TERMINAL);
        map.registerSprite(manager, LOCATION_OVERLAY_MODULE_TIMER);
    }

    // --------------------------------------------------------------------- //

    public TextureLoader() {
    }
}
