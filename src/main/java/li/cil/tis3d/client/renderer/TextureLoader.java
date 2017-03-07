package li.cil.tis3d.client.renderer;

import li.cil.tis3d.api.API;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class TextureLoader {
    public static final ResourceLocation LOCATION_BOOK_CODE_BACKGROUND = new ResourceLocation(API.MOD_ID, "textures/gui/book_code.png");
    public static final ResourceLocation LOCATION_MANUAL_BACKGROUND = new ResourceLocation(API.MOD_ID, "textures/gui/manual.png");
    public static final ResourceLocation LOCATION_MANUAL_TAB = new ResourceLocation(API.MOD_ID, "textures/gui/manual_tab.png");
    public static final ResourceLocation LOCATION_MANUAL_SCROLL = new ResourceLocation(API.MOD_ID, "textures/gui/manual_scroll.png");
    public static final ResourceLocation LOCATION_MANUAL_MISSING = new ResourceLocation(API.MOD_ID, "textures/gui/manual_missing.png");

    public static final ResourceLocation LOCATION_CASING_LOCKED_OVERLAY = new ResourceLocation(API.MOD_ID, "blocks/overlay/casing_locked");
    public static final ResourceLocation LOCATION_CASING_UNLOCKED_OVERLAY = new ResourceLocation(API.MOD_ID, "blocks/overlay/casing_unlocked");
    public static final ResourceLocation LOCATION_CASING_PORT_CLOSED_OVERLAY = new ResourceLocation(API.MOD_ID, "blocks/overlay/casing_port_closed");
    public static final ResourceLocation LOCATION_CASING_PORT_OPEN_OVERLAY = new ResourceLocation(API.MOD_ID, "blocks/overlay/casing_port_open");
    public static final ResourceLocation LOCATION_CASING_PORT_HIGHLIGHT_OVERLAY = new ResourceLocation(API.MOD_ID, "blocks/overlay/casing_port_highlight");
    public static final ResourceLocation LOCATION_CASING_PORT_CLOSED_SMALL_OVERLAY = new ResourceLocation(API.MOD_ID, "blocks/overlay/casing_port_closed_small");
    public static final ResourceLocation LOCATION_MODULE_AUDIO_OVERLAY = new ResourceLocation(API.MOD_ID, "blocks/overlay/module_audio");
    public static final ResourceLocation LOCATION_MODULE_BUNDLED_REDSTONE_OVERLAY = new ResourceLocation(API.MOD_ID, "blocks/overlay/module_bundled_redstone");
    public static final ResourceLocation LOCATION_MODULE_EXECUTION_OVERLAY_ERROR = new ResourceLocation(API.MOD_ID, "blocks/overlay/module_execution_error");
    public static final ResourceLocation LOCATION_MODULE_EXECUTION_OVERLAY_IDLE = new ResourceLocation(API.MOD_ID, "blocks/overlay/module_execution_idle");
    public static final ResourceLocation LOCATION_MODULE_EXECUTION_OVERLAY_RUNNING = new ResourceLocation(API.MOD_ID, "blocks/overlay/module_execution_running");
    public static final ResourceLocation LOCATION_MODULE_EXECUTION_OVERLAY_WAITING = new ResourceLocation(API.MOD_ID, "blocks/overlay/module_execution_waiting");
    public static final ResourceLocation LOCATION_MODULE_INFRARED_OVERLAY = new ResourceLocation(API.MOD_ID, "blocks/overlay/module_infrared");
    public static final ResourceLocation LOCATION_MODULE_KEYPAD_OVERLAY = new ResourceLocation(API.MOD_ID, "blocks/overlay/module_keypad");
    public static final ResourceLocation LOCATION_MODULE_QUEUE_OVERLAY = new ResourceLocation(API.MOD_ID, "blocks/overlay/module_queue");
    public static final ResourceLocation LOCATION_MODULE_RANDOM_OVERLAY = new ResourceLocation(API.MOD_ID, "blocks/overlay/module_random");
    public static final ResourceLocation LOCATION_MODULE_REDSTONE_OVERLAY = new ResourceLocation(API.MOD_ID, "blocks/overlay/module_redstone");
    public static final ResourceLocation LOCATION_MODULE_REDSTONE_BARS_OVERLAY = new ResourceLocation(API.MOD_ID, "blocks/overlay/module_redstone_bars");
    public static final ResourceLocation LOCATION_MODULE_SERIAL_PORT_OVERLAY = new ResourceLocation(API.MOD_ID, "blocks/overlay/module_serial_port");
    public static final ResourceLocation LOCATION_MODULE_STACK_OVERLAY = new ResourceLocation(API.MOD_ID, "blocks/overlay/module_stack");
    public static final ResourceLocation LOCATION_MODULE_TERMINAL_OVERLAY = new ResourceLocation(API.MOD_ID, "blocks/overlay/module_terminal");

    public static final TextureLoader INSTANCE = new TextureLoader();

    @SubscribeEvent
    public void onTextureStitchPre(final TextureStitchEvent.Pre event) {
        final TextureMap map = event.getMap();
        map.registerSprite(LOCATION_CASING_LOCKED_OVERLAY);
        map.registerSprite(LOCATION_CASING_UNLOCKED_OVERLAY);
        map.registerSprite(LOCATION_CASING_PORT_CLOSED_OVERLAY);
        map.registerSprite(LOCATION_CASING_PORT_OPEN_OVERLAY);
        map.registerSprite(LOCATION_CASING_PORT_HIGHLIGHT_OVERLAY);
        map.registerSprite(LOCATION_CASING_PORT_CLOSED_SMALL_OVERLAY);
        map.registerSprite(LOCATION_MODULE_AUDIO_OVERLAY);
        map.registerSprite(LOCATION_MODULE_BUNDLED_REDSTONE_OVERLAY);
        map.registerSprite(LOCATION_MODULE_EXECUTION_OVERLAY_ERROR);
        map.registerSprite(LOCATION_MODULE_EXECUTION_OVERLAY_IDLE);
        map.registerSprite(LOCATION_MODULE_EXECUTION_OVERLAY_RUNNING);
        map.registerSprite(LOCATION_MODULE_EXECUTION_OVERLAY_WAITING);
        map.registerSprite(LOCATION_MODULE_INFRARED_OVERLAY);
        map.registerSprite(LOCATION_MODULE_KEYPAD_OVERLAY);
        map.registerSprite(LOCATION_MODULE_QUEUE_OVERLAY);
        map.registerSprite(LOCATION_MODULE_RANDOM_OVERLAY);
        map.registerSprite(LOCATION_MODULE_REDSTONE_OVERLAY);
        map.registerSprite(LOCATION_MODULE_REDSTONE_BARS_OVERLAY);
        map.registerSprite(LOCATION_MODULE_SERIAL_PORT_OVERLAY);
        map.registerSprite(LOCATION_MODULE_STACK_OVERLAY);
        map.registerSprite(LOCATION_MODULE_TERMINAL_OVERLAY);
    }

    // --------------------------------------------------------------------- //

    private TextureLoader() {
    }
}
