package li.cil.tis3d.common.item;

import dev.architectury.injectables.annotations.ExpectPlatform;

public final class FacadeModuleItem extends ModuleItem {
    public FacadeModuleItem() {
        super(isEnabled() ? createProperties() : new Properties());
    }

    /**
     * This class only exists for this hook, so we can disable the module on Fabric if Sodium is present.
     */
    @ExpectPlatform
    private static boolean isEnabled() {
        throw new AssertionError();
    }
}
