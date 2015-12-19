package li.cil.tis3d.client.render;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import li.cil.tis3d.api.API;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;

public final class TextureLoader {
    public static final ResourceLocation LOCATION_MODULE_AUDIO_OVERLAY = new ResourceLocation(API.MOD_ID, "overlay/moduleAudio");
    public static final ResourceLocation LOCATION_MODULE_EXECUTION_OVERLAY_ERROR = new ResourceLocation(API.MOD_ID, "overlay/moduleExecutionError");
    public static final ResourceLocation LOCATION_MODULE_EXECUTION_OVERLAY_RUNNING = new ResourceLocation(API.MOD_ID, "overlay/moduleExecutionRunning");
    public static final ResourceLocation LOCATION_MODULE_EXECUTION_OVERLAY_WAITING = new ResourceLocation(API.MOD_ID, "overlay/moduleExecutionWaiting");
    public static final ResourceLocation LOCATION_MODULE_INFRARED_OVERLAY = new ResourceLocation(API.MOD_ID, "overlay/moduleInfrared");
    public static final ResourceLocation LOCATION_MODULE_RANDOM_OVERLAY = new ResourceLocation(API.MOD_ID, "overlay/moduleRandom");
    private static final ResourceLocation LOCATION_CASING_MODULE = new ResourceLocation(API.MOD_ID, "casingModule");

    public static final TextureLoader INSTANCE = new TextureLoader();
    public static IIcon ICON_CASING_MODULE;

    @SubscribeEvent
    public void onTextureStitchPre(final TextureStitchEvent.Pre event) {
        if (event.map.getTextureType() != 0) {
            return;
        }

        event.map.registerIcon(LOCATION_MODULE_EXECUTION_OVERLAY_ERROR.toString());
        event.map.registerIcon(LOCATION_MODULE_EXECUTION_OVERLAY_RUNNING.toString());
        event.map.registerIcon(LOCATION_MODULE_EXECUTION_OVERLAY_WAITING.toString());
        event.map.registerIcon(LOCATION_MODULE_AUDIO_OVERLAY.toString());
        event.map.registerIcon(LOCATION_MODULE_INFRARED_OVERLAY.toString());
        event.map.registerIcon(LOCATION_MODULE_RANDOM_OVERLAY.toString());
        ICON_CASING_MODULE = event.map.registerIcon(LOCATION_CASING_MODULE.toString());
    }

    // --------------------------------------------------------------------- //

    private TextureLoader() {
    }
}
