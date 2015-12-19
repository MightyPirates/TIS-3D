package li.cil.tis3d.common.integration;

import li.cil.tis3d.common.integration.charsetwires.ModCharsetWires;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

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
        proxies.add(new ModCharsetWires());
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
