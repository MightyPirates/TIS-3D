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
        proxies.add(new ProxyMinecraft());
    }

    // --------------------------------------------------------------------- //

    private Integration() {
    }

    public static void init() {
        proxies.stream().filter(ModProxy::isAvailable).forEach(ModProxy::init);
    }
}
