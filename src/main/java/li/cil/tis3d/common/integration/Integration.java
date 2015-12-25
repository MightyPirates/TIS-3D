package li.cil.tis3d.common.integration;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import li.cil.tis3d.common.integration.bluepower.ProxyBluePower;
import li.cil.tis3d.common.integration.minecraft.ProxyMinecraft;
import li.cil.tis3d.common.integration.projectred.ProxyProjectRed;
import li.cil.tis3d.common.integration.redlogic.ProxyRedLogic;

import java.util.ArrayList;
import java.util.List;

/**
 * Central registry tracking mod proxies and initializing them.
 */
public final class Integration {
    private static final List<ModProxy> proxies = new ArrayList<>();

    static {
        proxies.add(new ProxyBluePower());
        proxies.add(new ProxyProjectRed());
        proxies.add(new ProxyRedLogic());
        proxies.add(new ProxyMinecraft());
    }

    // --------------------------------------------------------------------- //

    public static void preInit(final FMLPreInitializationEvent event) {
        proxies.stream().filter(ModProxy::isAvailable).forEach(proxy -> proxy.preInit(event));
    }

    public static void init(final FMLInitializationEvent event) {
        proxies.stream().filter(ModProxy::isAvailable).forEach(proxy -> proxy.init(event));
    }

    public static void postInit(final FMLPostInitializationEvent event) {
        proxies.stream().filter(ModProxy::isAvailable).forEach(proxy -> proxy.postInit(event));
    }

    private Integration() {
    }
}
