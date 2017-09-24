package li.cil.tis3d.client.renderer;

import li.cil.tis3d.api.API;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class TextureLoader {
    public static final ResourceLocation LOCATION_GUI_BOOK_CODE_BACKGROUND = new ResourceLocation(API.MOD_ID, "textures/gui/book_code.png");
    public static final ResourceLocation LOCATION_GUI_MANUAL_BACKGROUND = new ResourceLocation(API.MOD_ID, "textures/gui/manual.png");
    public static final ResourceLocation LOCATION_GUI_MANUAL_TAB = new ResourceLocation(API.MOD_ID, "textures/gui/manual_tab.png");
    public static final ResourceLocation LOCATION_GUI_MANUAL_SCROLL = new ResourceLocation(API.MOD_ID, "textures/gui/manual_scroll.png");
    public static final ResourceLocation LOCATION_GUI_MANUAL_MISSING = new ResourceLocation(API.MOD_ID, "textures/gui/manual_missing.png");
    public static final ResourceLocation LOCATION_GUI_MEMORY = new ResourceLocation(API.MOD_ID, "textures/gui/module_memory.png");

    public static final ResourceLocation LOCATION_OVERLAY_CASING_LOCKED = new ResourceLocation(API.MOD_ID, "blocks/overlay/casing_locked");
    public static final ResourceLocation LOCATION_OVERLAY_CASING_UNLOCKED = new ResourceLocation(API.MOD_ID, "blocks/overlay/casing_unlocked");
    public static final ResourceLocation LOCATION_OVERLAY_CASING_PORT_CLOSED = new ResourceLocation(API.MOD_ID, "blocks/overlay/casing_port_closed");
    public static final ResourceLocation LOCATION_OVERLAY_CASING_PORT_OPEN = new ResourceLocation(API.MOD_ID, "blocks/overlay/casing_port_open");
    public static final ResourceLocation LOCATION_OVERLAY_CASING_PORT_HIGHLIGHT = new ResourceLocation(API.MOD_ID, "blocks/overlay/casing_port_highlight");
    public static final ResourceLocation LOCATION_OVERLAY_CASING_PORT_CLOSED_SMALL = new ResourceLocation(API.MOD_ID, "blocks/overlay/casing_port_closed_small");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_AUDIO = new ResourceLocation(API.MOD_ID, "blocks/overlay/module_audio");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_BUNDLED_REDSTONE = new ResourceLocation(API.MOD_ID, "blocks/overlay/module_bundled_redstone");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_EXECUTION_ERROR = new ResourceLocation(API.MOD_ID, "blocks/overlay/module_execution_error");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_EXECUTION_IDLE = new ResourceLocation(API.MOD_ID, "blocks/overlay/module_execution_idle");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_EXECUTION_RUNNING = new ResourceLocation(API.MOD_ID, "blocks/overlay/module_execution_running");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_EXECUTION_WAITING = new ResourceLocation(API.MOD_ID, "blocks/overlay/module_execution_waiting");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_INFRARED = new ResourceLocation(API.MOD_ID, "blocks/overlay/module_infrared");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_KEYPAD = new ResourceLocation(API.MOD_ID, "blocks/overlay/module_keypad");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_QUEUE = new ResourceLocation(API.MOD_ID, "blocks/overlay/module_queue");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_RANDOM = new ResourceLocation(API.MOD_ID, "blocks/overlay/module_random");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_REDSTONE = new ResourceLocation(API.MOD_ID, "blocks/overlay/module_redstone");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_REDSTONE_BARS = new ResourceLocation(API.MOD_ID, "blocks/overlay/module_redstone_bars");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_SERIAL_PORT = new ResourceLocation(API.MOD_ID, "blocks/overlay/module_serial_port");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_STACK = new ResourceLocation(API.MOD_ID, "blocks/overlay/module_stack");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_TERMINAL = new ResourceLocation(API.MOD_ID, "blocks/overlay/module_terminal");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_TIMER = new ResourceLocation(API.MOD_ID, "blocks/overlay/module_timer");

    public static final TextureLoader INSTANCE = new TextureLoader();

    @SubscribeEvent
    public void onTextureStitchPre(final TextureStitchEvent.Pre event) {
        final TextureMap map = event.getMap();
        map.registerSprite(LOCATION_OVERLAY_CASING_LOCKED);
        map.registerSprite(LOCATION_OVERLAY_CASING_UNLOCKED);
        map.registerSprite(LOCATION_OVERLAY_CASING_PORT_CLOSED);
        map.registerSprite(LOCATION_OVERLAY_CASING_PORT_OPEN);
        map.registerSprite(LOCATION_OVERLAY_CASING_PORT_HIGHLIGHT);
        map.registerSprite(LOCATION_OVERLAY_CASING_PORT_CLOSED_SMALL);
        map.registerSprite(LOCATION_OVERLAY_MODULE_AUDIO);
        map.registerSprite(LOCATION_OVERLAY_MODULE_BUNDLED_REDSTONE);
        map.registerSprite(LOCATION_OVERLAY_MODULE_EXECUTION_ERROR);
        map.registerSprite(LOCATION_OVERLAY_MODULE_EXECUTION_IDLE);
        map.registerSprite(LOCATION_OVERLAY_MODULE_EXECUTION_RUNNING);
        map.registerSprite(LOCATION_OVERLAY_MODULE_EXECUTION_WAITING);
        map.registerSprite(LOCATION_OVERLAY_MODULE_INFRARED);
        map.registerSprite(LOCATION_OVERLAY_MODULE_KEYPAD);
        map.registerSprite(LOCATION_OVERLAY_MODULE_QUEUE);
        map.registerSprite(LOCATION_OVERLAY_MODULE_RANDOM);
        map.registerSprite(LOCATION_OVERLAY_MODULE_REDSTONE);
        map.registerSprite(LOCATION_OVERLAY_MODULE_REDSTONE_BARS);
        map.registerSprite(LOCATION_OVERLAY_MODULE_SERIAL_PORT);
        map.registerSprite(LOCATION_OVERLAY_MODULE_STACK);
        map.registerSprite(LOCATION_OVERLAY_MODULE_TERMINAL);
        map.registerSprite(LOCATION_OVERLAY_MODULE_TIMER);
    }

    // --------------------------------------------------------------------- //

    private TextureLoader() {
    }
}
