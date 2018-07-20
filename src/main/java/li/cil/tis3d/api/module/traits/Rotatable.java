package li.cil.tis3d.api.module.traits;

import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Module;

/**
 * Implement this on your module to indicate it can be rotated.
 * <p>
 * TIS-3D will use this to let your module know which way it should face after
 * being placed into a casing. Note that this is only called when the module
 * is placed in the top or bottom slot of a casing.
 * <p>
 * The set orientation represents where {@link Port#UP} is pointing.
 */
public interface Rotatable extends Module {
    /**
     * Get the current orientation of the module.
     *
     * @return the current orientation.
     */
    Port getFacing();

    /**
     * Set the orientation of the module.
     *
     * @param port the orientation to set.
     */
    void setFacing(final Port port);
}
