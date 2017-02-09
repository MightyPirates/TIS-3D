package li.cil.tis3d.common;

import li.cil.tis3d.api.API;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

/**
 * Entry point for FML.
 */
@Mod(modid = API.MOD_ID, version = API.MOD_VERSION, useMetadata = true)
public final class TIS3D {
    // --------------------------------------------------------------------- //
    // FML / Forge

    @Mod.Instance(API.MOD_ID)
    public static TIS3D instance;

    @SidedProxy(clientSide = Constants.PROXY_CLIENT, serverSide = Constants.PROXY_COMMON)
    public static ProxyCommon proxy;

    @EventHandler
    public void onPreInit(final FMLPreInitializationEvent event) {
        log = event.getModLog();
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

    @EventHandler
    public void onMissingMappings(final FMLMissingMappingsEvent event) {
        proxy.onMissingMappings(event);
    }

    // --------------------------------------------------------------------- //

    /**
     * Logger the mod should use, filled in pre-init.
     */
    private static Logger log;

    /**
     * Get the logger to be used by the mod.
     *
     * @return the mod's logger.
     */
    public static Logger getLog() {
        return log;
    }
}
