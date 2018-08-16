package li.cil.tis3d.common.integration;

public interface ModProxy {
    boolean isAvailable();

    default void init() {

    }

    // TODO
    /* default void preInit(final FMLPreInitializationEvent event) {
    }

    default void init(final FMLInitializationEvent event) {
    }

    default void postInit(final FMLPostInitializationEvent event) {
    } */
}
