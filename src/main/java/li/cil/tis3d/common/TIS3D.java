package li.cil.tis3d.common;

import li.cil.tis3d.api.API;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Entry point for FML.
 */
//@Mod(modid = API.MOD_ID, version = API.MOD_VERSION, useMetadata = true)
public final class TIS3D {
    // --------------------------------------------------------------------- //
    // FML / Forge

 /*   @Mod.Instance(API.MOD_ID)
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
        if (log == null) {
            log = LogManager.getLogger();
        }
        return log;
    }
}
