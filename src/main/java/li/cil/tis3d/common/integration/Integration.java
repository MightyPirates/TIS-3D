package li.cil.tis3d.common.integration;

import li.cil.tis3d.common.integration.minecraft.ProxyMinecraft;

import java.util.ArrayList;
import java.util.List;

/**
 * Central registry tracking mod proxies and initializing them.
 */
public final class Integration {
    private static final List<ModProxy> proxies = new ArrayList<>();

    static {
    	// TODO
        // proxies.add(new ProxyCharsetWires());
        proxies.add(new ProxyMinecraft());
    }

    // --------------------------------------------------------------------- //

    /* public static void preInit(final FMLPreInitializationEvent event) {
        proxies.stream().filter(ModProxy::isAvailable).forEach(proxy -> proxy.preInit(event));
    }

    public static void init(final FMLInitializationEvent event) {
        proxies.stream().filter(ModProxy::isAvailable).forEach(proxy -> proxy.init(event));
    }

    public static void postInit(final FMLPostInitializationEvent event) {
        proxies.stream().filter(ModProxy::isAvailable).forEach(proxy -> proxy.postInit(event));
    } */

    private Integration() {
    }

	public static void init() {
		proxies.stream().filter(ModProxy::isAvailable).forEach(ModProxy::init);
	}
}
