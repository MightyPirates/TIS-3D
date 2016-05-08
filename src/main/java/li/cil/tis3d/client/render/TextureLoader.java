package li.cil.tis3d.client.render;

import li.cil.tis3d.api.API;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class TextureLoader {
    public static final ResourceLocation LOCATION_CASING_LOCKED_OVERLAY = new ResourceLocation(API.MOD_ID, "blocks/overlay/casingLocked");
    public static final ResourceLocation LOCATION_CASING_UNLOCKED_OVERLAY = new ResourceLocation(API.MOD_ID, "blocks/overlay/casingUnlocked");
    public static final ResourceLocation LOCATION_MODULE_AUDIO_OVERLAY = new ResourceLocation(API.MOD_ID, "blocks/overlay/moduleAudio");
    public static final ResourceLocation LOCATION_MODULE_EXECUTION_OVERLAY_ERROR = new ResourceLocation(API.MOD_ID, "blocks/overlay/moduleExecutionError");
    public static final ResourceLocation LOCATION_MODULE_EXECUTION_OVERLAY_IDLE = new ResourceLocation(API.MOD_ID, "blocks/overlay/moduleExecutionIdle");
    public static final ResourceLocation LOCATION_MODULE_EXECUTION_OVERLAY_RUNNING = new ResourceLocation(API.MOD_ID, "blocks/overlay/moduleExecutionRunning");
    public static final ResourceLocation LOCATION_MODULE_EXECUTION_OVERLAY_WAITING = new ResourceLocation(API.MOD_ID, "blocks/overlay/moduleExecutionWaiting");
    public static final ResourceLocation LOCATION_MODULE_INFRARED_OVERLAY = new ResourceLocation(API.MOD_ID, "blocks/overlay/moduleInfrared");
    public static final ResourceLocation LOCATION_MODULE_RANDOM_OVERLAY = new ResourceLocation(API.MOD_ID, "blocks/overlay/moduleRandom");

    public static final TextureLoader INSTANCE = new TextureLoader();

    @SubscribeEvent
    public void onTextureStitchPre(final TextureStitchEvent.Pre event) {
        event.getMap().registerSprite(LOCATION_CASING_LOCKED_OVERLAY);
        event.getMap().registerSprite(LOCATION_CASING_UNLOCKED_OVERLAY);
        event.getMap().registerSprite(LOCATION_MODULE_AUDIO_OVERLAY);
        event.getMap().registerSprite(LOCATION_MODULE_EXECUTION_OVERLAY_ERROR);
        event.getMap().registerSprite(LOCATION_MODULE_EXECUTION_OVERLAY_IDLE);
        event.getMap().registerSprite(LOCATION_MODULE_EXECUTION_OVERLAY_RUNNING);
        event.getMap().registerSprite(LOCATION_MODULE_EXECUTION_OVERLAY_WAITING);
        event.getMap().registerSprite(LOCATION_MODULE_INFRARED_OVERLAY);
        event.getMap().registerSprite(LOCATION_MODULE_RANDOM_OVERLAY);
    }

    // --------------------------------------------------------------------- //

    private TextureLoader() {
    }
}
