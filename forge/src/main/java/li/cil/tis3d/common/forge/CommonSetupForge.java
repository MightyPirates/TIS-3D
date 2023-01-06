package li.cil.tis3d.common.forge;

import li.cil.tis3d.api.API;
import li.cil.tis3d.common.CommonSetup;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = API.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CommonSetupForge {
    @SubscribeEvent
    public static void handleCommonSetup(final FMLCommonSetupEvent event) {
        CommonSetup.run();
    }
}
