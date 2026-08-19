package li.cil.tis3d.common.neoforge;

import li.cil.tis3d.api.API;
import li.cil.tis3d.client.manual.Manuals;
import li.cil.tis3d.common.CommonBootstrap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(API.MOD_ID)
public class BootstrapNeoForge {
    public static ModContainer MOD_CONTAINER;

    public BootstrapNeoForge(final ModContainer modContainer) {
        MOD_CONTAINER = modContainer;
        CommonBootstrap.run();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            Manuals.initialize();
        }
    }
}
