package li.cil.tis3d.client.renderer;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import li.cil.tis3d.api.API;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;

public final class TextureLoader {
    public static final ResourceLocation LOCATION_BOOK_CODE_BACKGROUND = new ResourceLocation(API.MOD_ID, "textures/gui/bookCode.png");
    public static final ResourceLocation LOCATION_MANUAL_BACKGROUND = new ResourceLocation(API.MOD_ID, "textures/gui/manual.png");
    public static final ResourceLocation LOCATION_MANUAL_TAB = new ResourceLocation(API.MOD_ID, "textures/gui/manualTab.png");
    public static final ResourceLocation LOCATION_MANUAL_SCROLL = new ResourceLocation(API.MOD_ID, "textures/gui/manualScroll.png");
    public static final ResourceLocation LOCATION_MANUAL_MISSING = new ResourceLocation(API.MOD_ID, "textures/gui/manualMissing.png");

    public static final ResourceLocation LOCATION_CASING_LOCKED_OVERLAY = new ResourceLocation(API.MOD_ID, "overlay/casingLocked");
    public static final ResourceLocation LOCATION_CASING_UNLOCKED_OVERLAY = new ResourceLocation(API.MOD_ID, "overlay/casingUnlocked");
    public static final ResourceLocation LOCATION_CASING_PORT_CLOSED_OVERLAY = new ResourceLocation(API.MOD_ID, "overlay/casing_port_closed");
    public static final ResourceLocation LOCATION_CASING_PORT_OPEN_OVERLAY = new ResourceLocation(API.MOD_ID, "overlay/casing_port_open");
    public static final ResourceLocation LOCATION_CASING_PORT_HIGHLIGHT_OVERLAY = new ResourceLocation(API.MOD_ID, "overlay/casing_port_highlight");
    public static final ResourceLocation LOCATION_CASING_PORT_CLOSED_SMALL_OVERLAY = new ResourceLocation(API.MOD_ID, "overlay/casing_port_closed_small");
    public static final ResourceLocation LOCATION_CASING_MODULE = new ResourceLocation(API.MOD_ID, "casingModule");
    public static final ResourceLocation LOCATION_MODULE_AUDIO_OVERLAY = new ResourceLocation(API.MOD_ID, "overlay/moduleAudio");
    public static final ResourceLocation LOCATION_MODULE_BUNDLED_REDSTONE_OVERLAY = new ResourceLocation(API.MOD_ID, "overlay/moduleBundledRedstone");
    public static final ResourceLocation LOCATION_MODULE_EXECUTION_OVERLAY_ERROR = new ResourceLocation(API.MOD_ID, "overlay/moduleExecutionError");
    public static final ResourceLocation LOCATION_MODULE_EXECUTION_OVERLAY_IDLE = new ResourceLocation(API.MOD_ID, "overlay/moduleExecutionIdle");
    public static final ResourceLocation LOCATION_MODULE_EXECUTION_OVERLAY_RUNNING = new ResourceLocation(API.MOD_ID, "overlay/moduleExecutionRunning");
    public static final ResourceLocation LOCATION_MODULE_EXECUTION_OVERLAY_WAITING = new ResourceLocation(API.MOD_ID, "overlay/moduleExecutionWaiting");
    public static final ResourceLocation LOCATION_MODULE_INFRARED_OVERLAY = new ResourceLocation(API.MOD_ID, "overlay/moduleInfrared");
    public static final ResourceLocation LOCATION_MODULE_KEYPAD_OVERLAY = new ResourceLocation(API.MOD_ID, "overlay/moduleKeypad");
    public static final ResourceLocation LOCATION_MODULE_QUEUE_OVERLAY = new ResourceLocation(API.MOD_ID, "overlay/moduleQueue");
    public static final ResourceLocation LOCATION_MODULE_RANDOM_OVERLAY = new ResourceLocation(API.MOD_ID, "overlay/moduleRandom");
    public static final ResourceLocation LOCATION_MODULE_REDSTONE_OVERLAY = new ResourceLocation(API.MOD_ID, "overlay/moduleRedstone");
    public static final ResourceLocation LOCATION_MODULE_REDSTONE_BARS_OVERLAY = new ResourceLocation(API.MOD_ID, "overlay/moduleRedstoneBars");
    public static final ResourceLocation LOCATION_MODULE_SERIAL_PORT_OVERLAY = new ResourceLocation(API.MOD_ID, "overlay/moduleSerialPort");
    public static final ResourceLocation LOCATION_MODULE_STACK_OVERLAY = new ResourceLocation(API.MOD_ID, "overlay/moduleStack");
    public static final ResourceLocation LOCATION_MODULE_TERMINAL_OVERLAY = new ResourceLocation(API.MOD_ID, "overlay/moduleTerminal");
    public static final ResourceLocation LOCATION_MODULE_TIMER_OVERLAY = new ResourceLocation(API.MOD_ID, "overlay/moduleTimer");

    public static final TextureLoader INSTANCE = new TextureLoader();

    @SubscribeEvent
    public void onTextureStitchPre(final TextureStitchEvent.Pre event) {
        if (event.map.getTextureType() != 0) {
            return;
        }
        event.map.registerIcon(LOCATION_CASING_LOCKED_OVERLAY.toString());
        event.map.registerIcon(LOCATION_CASING_UNLOCKED_OVERLAY.toString());
        event.map.registerIcon(LOCATION_CASING_PORT_CLOSED_OVERLAY.toString());
        event.map.registerIcon(LOCATION_CASING_PORT_OPEN_OVERLAY.toString());
        event.map.registerIcon(LOCATION_CASING_PORT_HIGHLIGHT_OVERLAY.toString());
        event.map.registerIcon(LOCATION_CASING_PORT_CLOSED_SMALL_OVERLAY.toString());
        event.map.registerIcon(LOCATION_CASING_MODULE.toString());
        event.map.registerIcon(LOCATION_MODULE_AUDIO_OVERLAY.toString());
        event.map.registerIcon(LOCATION_MODULE_BUNDLED_REDSTONE_OVERLAY.toString());
        event.map.registerIcon(LOCATION_MODULE_EXECUTION_OVERLAY_ERROR.toString());
        event.map.registerIcon(LOCATION_MODULE_EXECUTION_OVERLAY_IDLE.toString());
        event.map.registerIcon(LOCATION_MODULE_EXECUTION_OVERLAY_RUNNING.toString());
        event.map.registerIcon(LOCATION_MODULE_EXECUTION_OVERLAY_WAITING.toString());
        event.map.registerIcon(LOCATION_MODULE_INFRARED_OVERLAY.toString());
        event.map.registerIcon(LOCATION_MODULE_KEYPAD_OVERLAY.toString());
        event.map.registerIcon(LOCATION_MODULE_QUEUE_OVERLAY.toString());
        event.map.registerIcon(LOCATION_MODULE_RANDOM_OVERLAY.toString());
        event.map.registerIcon(LOCATION_MODULE_REDSTONE_OVERLAY.toString());
        event.map.registerIcon(LOCATION_MODULE_REDSTONE_BARS_OVERLAY.toString());
        event.map.registerIcon(LOCATION_MODULE_SERIAL_PORT_OVERLAY.toString());
        event.map.registerIcon(LOCATION_MODULE_STACK_OVERLAY.toString());
        event.map.registerIcon(LOCATION_MODULE_TERMINAL_OVERLAY.toString());
        event.map.registerIcon(LOCATION_MODULE_TIMER_OVERLAY.toString());
    }

    // --------------------------------------------------------------------- //

    private TextureLoader() {
    }
}
