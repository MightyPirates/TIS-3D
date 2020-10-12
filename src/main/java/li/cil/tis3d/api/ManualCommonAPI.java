package li.cil.tis3d.api;

import li.cil.tis3d.api.manual.PathProvider;

import javax.annotation.Nullable;

/**
 * This API allows interfacing with the in-game manual of TIS-3D.
 * <p>
 * It allows opening the manual at a desired specific page, as well as
 * registering custom tabs and content callback handlers.
 */
public interface ManualCommonAPI {
    /**
     * Register a path provider.
     * <p>
     * Path providers are used to find documentation entries for item stacks
     * and blocks in the world.
     *
     * @param provider the provider to register.
     */
    void addProvider(final PathProvider provider);
}
