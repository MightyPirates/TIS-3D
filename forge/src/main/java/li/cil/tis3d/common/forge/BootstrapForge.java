package li.cil.tis3d.common.forge;

import dev.architectury.platform.forge.EventBuses;
import li.cil.tis3d.api.API;
import li.cil.tis3d.client.ClientBootstrap;
import li.cil.tis3d.client.manual.Manuals;
import li.cil.tis3d.common.CommonBootstrap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(API.MOD_ID)
public class BootstrapForge {
    public BootstrapForge() {
        EventBuses.registerModEventBus(API.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        CommonBootstrap.run();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            Manuals.initialize();
            ClientBootstrap.run();
        });
    }
}
