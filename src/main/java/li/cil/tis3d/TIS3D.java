package li.cil.tis3d;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import li.cil.tis3d.api.API;
import li.cil.tis3d.common.ProxyCommon;
import org.apache.logging.log4j.Logger;

/**
 * Entry point for FML.
 */
@Mod(modid = API.MOD_ID, version = API.MOD_VERSION)
public final class TIS3D {
    // --------------------------------------------------------------------- //
    // FML / Forge

    @SidedProxy(clientSide = Constants.PROXY_CLIENT, serverSide = Constants.PROXY_COMMON)
    public static ProxyCommon proxy;

    @Mod.EventHandler
    public void onPreInit(final FMLPreInitializationEvent event) {
        log = event.getModLog();
        proxy.onPreInit(event);
    }

    @Mod.EventHandler
    public void onInit(final FMLInitializationEvent event) {
        proxy.onInit(event);
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
