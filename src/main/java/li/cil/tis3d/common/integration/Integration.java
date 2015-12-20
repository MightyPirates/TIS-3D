package li.cil.tis3d.common.integration;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import li.cil.tis3d.common.integration.bluepower.ProxyBluePower;
import li.cil.tis3d.common.integration.minecraft.ProxyMinecraft;
import li.cil.tis3d.common.integration.redlogic.ProxyRedLogic;

import java.util.ArrayList;
import java.util.List;

/**
 * Central registry tracking mod proxies and initializing them.
 */
public final class Integration {
    public static final Integration INSTANCE = new Integration();

    // --------------------------------------------------------------------- //

    private final List<ModProxy> proxies = new ArrayList<>();

    private Integration() {
        proxies.add(new ProxyBluePower());
        proxies.add(new ProxyRedLogic());
        proxies.add(new ProxyMinecraft());
    }

    // --------------------------------------------------------------------- //

    public void preInit(final FMLPreInitializationEvent event) {
        proxies.stream().filter(ModProxy::isAvailable).forEach(proxy -> proxy.preInit(event));
    }

    public void init(final FMLInitializationEvent event) {
        proxies.stream().filter(ModProxy::isAvailable).forEach(proxy -> proxy.init(event));
    }

    public void postInit(final FMLPostInitializationEvent event) {
        proxies.stream().filter(ModProxy::isAvailable).forEach(proxy -> proxy.postInit(event));
    }
}
