package li.cil.tis3d.client.renderer;

import li.cil.tis3d.api.API;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;

@OnlyIn(Dist.CLIENT)
public final class Textures {
    public static final ResourceLocation LOCATION_GUI_BOOK_CODE_BACKGROUND = new ResourceLocation(API.MOD_ID, "textures/gui/code_book.png");
    public static final ResourceLocation LOCATION_GUI_MANUAL_BACKGROUND = new ResourceLocation(API.MOD_ID, "textures/gui/manual.png");
    public static final ResourceLocation LOCATION_GUI_MANUAL_TAB = new ResourceLocation(API.MOD_ID, "textures/gui/manual_tab.png");
    public static final ResourceLocation LOCATION_GUI_MANUAL_SCROLL = new ResourceLocation(API.MOD_ID, "textures/gui/manual_scroll.png");
    public static final ResourceLocation LOCATION_GUI_MEMORY = new ResourceLocation(API.MOD_ID, "textures/gui/module_memory.png");

    public static final ResourceLocation LOCATION_OVERLAY_CASING_LOCKED = new ResourceLocation(API.MOD_ID, "block/overlay/casing_locked");
    public static final ResourceLocation LOCATION_OVERLAY_CASING_UNLOCKED = new ResourceLocation(API.MOD_ID, "block/overlay/casing_unlocked");
    public static final ResourceLocation LOCATION_OVERLAY_CASING_PORT_CLOSED = new ResourceLocation(API.MOD_ID, "block/overlay/casing_port_closed");
    public static final ResourceLocation LOCATION_OVERLAY_CASING_PORT_OPEN = new ResourceLocation(API.MOD_ID, "block/overlay/casing_port_open");
    public static final ResourceLocation LOCATION_OVERLAY_CASING_PORT_HIGHLIGHT = new ResourceLocation(API.MOD_ID, "block/overlay/casing_port_highlight");
    public static final ResourceLocation LOCATION_OVERLAY_CASING_PORT_CLOSED_SMALL = new ResourceLocation(API.MOD_ID, "block/overlay/casing_port_closed_small");

    public static final ResourceLocation LOCATION_OVERLAY_MODULE_AUDIO = new ResourceLocation(API.MOD_ID, "block/overlay/audio_module");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_EXECUTION_ERROR = new ResourceLocation(API.MOD_ID, "block/overlay/execution_module_error");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_EXECUTION_IDLE = new ResourceLocation(API.MOD_ID, "block/overlay/execution_module_idle");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_EXECUTION_RUNNING = new ResourceLocation(API.MOD_ID, "block/overlay/execution_module_running");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_EXECUTION_WAITING = new ResourceLocation(API.MOD_ID, "block/overlay/execution_module_waiting");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_INFRARED = new ResourceLocation(API.MOD_ID, "block/overlay/infrared_module");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_KEYPAD = new ResourceLocation(API.MOD_ID, "block/overlay/keypad_module");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_QUEUE = new ResourceLocation(API.MOD_ID, "block/overlay/queue_module");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_RANDOM = new ResourceLocation(API.MOD_ID, "block/overlay/random_module");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_REDSTONE = new ResourceLocation(API.MOD_ID, "block/overlay/redstone_module");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_REDSTONE_BARS = new ResourceLocation(API.MOD_ID, "block/overlay/redstone_bars_module");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_SEQUENCER = new ResourceLocation(API.MOD_ID, "block/overlay/sequencer_module");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_SERIAL_PORT = new ResourceLocation(API.MOD_ID, "block/overlay/serial_port_module");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_STACK = new ResourceLocation(API.MOD_ID, "block/overlay/stack_module");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_TERMINAL = new ResourceLocation(API.MOD_ID, "block/overlay/terminal_module");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_TIMER = new ResourceLocation(API.MOD_ID, "block/overlay/timer_module");

    public static void handleTextureStitchEvent(final TextureStitchEvent.Pre event) {
        event.addSprite(LOCATION_OVERLAY_CASING_LOCKED);
        event.addSprite(LOCATION_OVERLAY_CASING_UNLOCKED);
        event.addSprite(LOCATION_OVERLAY_CASING_PORT_CLOSED);
        event.addSprite(LOCATION_OVERLAY_CASING_PORT_OPEN);
        event.addSprite(LOCATION_OVERLAY_CASING_PORT_HIGHLIGHT);
        event.addSprite(LOCATION_OVERLAY_CASING_PORT_CLOSED_SMALL);
        event.addSprite(LOCATION_OVERLAY_MODULE_AUDIO);
        event.addSprite(LOCATION_OVERLAY_MODULE_EXECUTION_ERROR);
        event.addSprite(LOCATION_OVERLAY_MODULE_EXECUTION_IDLE);
        event.addSprite(LOCATION_OVERLAY_MODULE_EXECUTION_RUNNING);
        event.addSprite(LOCATION_OVERLAY_MODULE_EXECUTION_WAITING);
        event.addSprite(LOCATION_OVERLAY_MODULE_INFRARED);
        event.addSprite(LOCATION_OVERLAY_MODULE_KEYPAD);
        event.addSprite(LOCATION_OVERLAY_MODULE_QUEUE);
        event.addSprite(LOCATION_OVERLAY_MODULE_RANDOM);
        event.addSprite(LOCATION_OVERLAY_MODULE_REDSTONE);
        event.addSprite(LOCATION_OVERLAY_MODULE_REDSTONE_BARS);
        event.addSprite(LOCATION_OVERLAY_MODULE_SEQUENCER);
        event.addSprite(LOCATION_OVERLAY_MODULE_SERIAL_PORT);
        event.addSprite(LOCATION_OVERLAY_MODULE_STACK);
        event.addSprite(LOCATION_OVERLAY_MODULE_TERMINAL);
        event.addSprite(LOCATION_OVERLAY_MODULE_TIMER);
    }

    // --------------------------------------------------------------------- //

    private Textures() {
    }
}
