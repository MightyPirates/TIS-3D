package li.cil.tis3d.api.module;

import li.cil.tis3d.api.machine.Casing;

/**
 * Modules implementing this will be queried by their {@link Casing}
 * for a redstone strength when the block is queried for its weak redstone power on
 * the side the module is installed in.
 */
public interface Redstone {
    /**
     * Get the current redstone output of the module.
     *
     * @return the current redstone output.
     */
    int getRedstoneOutput();
}
