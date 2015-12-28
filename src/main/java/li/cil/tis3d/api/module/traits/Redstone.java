package li.cil.tis3d.api.module.traits;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.module.Module;

/**
 * Modules implementing this will be queried by their {@link Casing}
 * for a redstone strength when the block is queried for its weak redstone power on
 * the side the module is installed in.
 */
public interface Redstone extends Module {
    /**
     * Get the current redstone output of the module.
     *
     * @return the current redstone output.
     */
    short getRedstoneOutput();

    /**
     * Set the new input value for the module.
     *
     * @param value the new input value of the module.
     */
    void setRedstoneInput(short value);
}
