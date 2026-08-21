/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.client.renderer;

import li.cil.tis3d.api.API;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class Textures {
    public static final ResourceLocation LOCATION_GUI_BOOK_CODE_BACKGROUND = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "textures/gui/code_book.png");
    public static final ResourceLocation LOCATION_GUI_MANUAL_BACKGROUND = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "textures/gui/manual.png");
    public static final ResourceLocation LOCATION_GUI_MANUAL_TAB = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "textures/gui/manual_tab.png");
    public static final ResourceLocation LOCATION_GUI_MANUAL_SCROLL = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "textures/gui/manual_scroll.png");
    public static final ResourceLocation LOCATION_GUI_MEMORY = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "textures/gui/module_memory.png");

    public static final ResourceLocation LOCATION_OVERLAY_CASING_LOCKED = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "block/overlay/casing_locked");
    public static final ResourceLocation LOCATION_OVERLAY_CASING_UNLOCKED = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "block/overlay/casing_unlocked");
    public static final ResourceLocation LOCATION_OVERLAY_CASING_PORT_CLOSED = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "block/overlay/casing_port_closed");
    public static final ResourceLocation LOCATION_OVERLAY_CASING_PORT_OPEN = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "block/overlay/casing_port_open");
    public static final ResourceLocation LOCATION_OVERLAY_CASING_PORT_HIGHLIGHT = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "block/overlay/casing_port_highlight");
    public static final ResourceLocation LOCATION_OVERLAY_CASING_PORT_CLOSED_SMALL = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "block/overlay/casing_port_closed_small");

    public static final ResourceLocation LOCATION_OVERLAY_MODULE_AUDIO = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "block/overlay/audio_module");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_EXECUTION_ERROR = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "block/overlay/execution_module_error");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_EXECUTION_IDLE = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "block/overlay/execution_module_idle");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_EXECUTION_RUNNING = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "block/overlay/execution_module_running");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_EXECUTION_WAITING = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "block/overlay/execution_module_waiting");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_INFRARED = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "block/overlay/infrared_module");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_KEYPAD = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "block/overlay/keypad_module");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_QUEUE = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "block/overlay/queue_module");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_RANDOM = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "block/overlay/random_module");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_REDSTONE = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "block/overlay/redstone_module");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_REDSTONE_BARS = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "block/overlay/redstone_bars_module");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_SEQUENCER = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "block/overlay/sequencer_module");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_SERIAL_PORT = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "block/overlay/serial_port_module");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_STACK = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "block/overlay/stack_module");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_TERMINAL = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "block/overlay/terminal_module");
    public static final ResourceLocation LOCATION_OVERLAY_MODULE_TIMER = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "block/overlay/timer_module");

    // --------------------------------------------------------------------- //

    private Textures() {
    }
}
