package li.cil.tis3d;

import li.cil.tis3d.common.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Constants.MOD_ID, version = Constants.MOD_VERSION)
public final class TIS3D {
    @SidedProxy(clientSide = "li.cil.tis3d.client.ClientProxy", serverSide = "li.cil.tis3d.common.CommonProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void onPreInit(final FMLPreInitializationEvent event) {
        proxy.onPreInit(event);
    }

    @EventHandler
    public void onInit(final FMLInitializationEvent event) {
        proxy.onInit(event);
    }

    @EventHandler
    public void onPostInit(final FMLPostInitializationEvent event) {
        proxy.onPostInit(event);
    }
}
