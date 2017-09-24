package li.cil.tis3d.client.renderer;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import li.cil.tis3d.api.API;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;

public final class TextureLoader {
    public static final ResourceLocation LOCATION_GUI_BOOK_CODE_BACKGROUND = new ResourceLocation(API.MOD_ID, "textures/gui/bookCode.png");
    public static final ResourceLocation LOCATION_GUI_MANUAL_BACKGROUND = new ResourceLocation(API.MOD_ID, "textures/gui/manual.png");
    public static final ResourceLocation LOCATION_GUI_MANUAL_TAB = new ResourceLocation(API.MOD_ID, "textures/gui/manualTab.png");
    public static final ResourceLocation LOCATION_GUI_MANUAL_SCROLL = new ResourceLocation(API.MOD_ID, "textures/gui/manualScroll.png");
    public static final ResourceLocation LOCATION_GUI_MANUAL_MISSING = new ResourceLocation(API.MOD_ID, "textures/gui/manualMissing.png");
    public static final ResourceLocation LOCATION_GUI_MEMORY = new ResourceLocation(API.MOD_ID, "textures/gui/module_memory.png");

    public static final ResourceLocation LOCATION_CASING_MODULE = new ResourceLocation(API.MOD_ID, "casingModule");
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
        if (event.map.getTextureType() != 0) {
            return;
        }
        event.map.registerIcon(LOCATION_CASING_MODULE.toString());
        event.map.registerIcon(LOCATION_OVERLAY_CASING_LOCKED.toString());
        event.map.registerIcon(LOCATION_OVERLAY_CASING_UNLOCKED.toString());
        event.map.registerIcon(LOCATION_OVERLAY_CASING_PORT_CLOSED.toString());
        event.map.registerIcon(LOCATION_OVERLAY_CASING_PORT_OPEN.toString());
        event.map.registerIcon(LOCATION_OVERLAY_CASING_PORT_HIGHLIGHT.toString());
        event.map.registerIcon(LOCATION_OVERLAY_CASING_PORT_CLOSED_SMALL.toString());
        event.map.registerIcon(LOCATION_OVERLAY_MODULE_AUDIO.toString());
        event.map.registerIcon(LOCATION_OVERLAY_MODULE_BUNDLED_REDSTONE.toString());
        event.map.registerIcon(LOCATION_OVERLAY_MODULE_EXECUTION_ERROR.toString());
        event.map.registerIcon(LOCATION_OVERLAY_MODULE_EXECUTION_IDLE.toString());
        event.map.registerIcon(LOCATION_OVERLAY_MODULE_EXECUTION_RUNNING.toString());
        event.map.registerIcon(LOCATION_OVERLAY_MODULE_EXECUTION_WAITING.toString());
        event.map.registerIcon(LOCATION_OVERLAY_MODULE_INFRARED.toString());
        event.map.registerIcon(LOCATION_OVERLAY_MODULE_KEYPAD.toString());
        event.map.registerIcon(LOCATION_OVERLAY_MODULE_QUEUE.toString());
        event.map.registerIcon(LOCATION_OVERLAY_MODULE_RANDOM.toString());
        event.map.registerIcon(LOCATION_OVERLAY_MODULE_REDSTONE.toString());
        event.map.registerIcon(LOCATION_OVERLAY_MODULE_REDSTONE_BARS.toString());
        event.map.registerIcon(LOCATION_OVERLAY_MODULE_SERIAL_PORT.toString());
        event.map.registerIcon(LOCATION_OVERLAY_MODULE_STACK.toString());
        event.map.registerIcon(LOCATION_OVERLAY_MODULE_TERMINAL.toString());
        event.map.registerIcon(LOCATION_OVERLAY_MODULE_TIMER.toString());
    }

    // --------------------------------------------------------------------- //

    private TextureLoader() {
    }
}
