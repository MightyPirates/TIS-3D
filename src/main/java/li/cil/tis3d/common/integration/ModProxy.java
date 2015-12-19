package li.cil.tis3d.common.integration;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public interface ModProxy {
    boolean isAvailable();

    default void preInit(final FMLPreInitializationEvent event) {
    }

    default void init(final FMLInitializationEvent event) {
    }

    default void postInit(final FMLPostInitializationEvent event) {
    }
}
