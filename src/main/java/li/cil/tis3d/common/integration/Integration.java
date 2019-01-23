package li.cil.tis3d.common.integration;

import li.cil.tis3d.common.integration.minecraft.MinecraftProxy;

import java.util.ArrayList;
import java.util.List;

/**
 * Central registry tracking mod proxies and initializing them.
 */
public final class Integration {
    private static final List<ModProxy> PROXIES = new ArrayList<>();

    static {
        PROXIES.add(new MinecraftProxy());
    }

    // --------------------------------------------------------------------- //

    public static void init() {
        PROXIES.stream().filter(ModProxy::isAvailable).forEach(ModProxy::init);
    }

    // --------------------------------------------------------------------- //

    private Integration() {
    }
}
