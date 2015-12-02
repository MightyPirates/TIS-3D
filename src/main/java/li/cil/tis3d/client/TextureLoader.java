package li.cil.tis3d.client;

import li.cil.tis3d.Constants;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class TextureLoader {
    public static final TextureLoader INSTANCE = new TextureLoader();
    public static final ResourceLocation LOCATION_MODULE_EXECUTION_OVERLAY = new ResourceLocation(Constants.MOD_ID, "blocks/overlay/moduleExecutable");

    @SubscribeEvent
    public void onTextureStitchPre(final TextureStitchEvent.Pre event) {
        event.map.registerSprite(LOCATION_MODULE_EXECUTION_OVERLAY);
    }

    private TextureLoader() {
    }
}
