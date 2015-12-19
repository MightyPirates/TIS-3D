package li.cil.tis3d.api.module;

/**
 * Modules implementing this will be queried for bundled redstone output when
 * necessary, and notified of bundled input changes.
 */
public interface BundledRedstone extends Module {
    /**
     * Get the current bundled redstone output of the module.
     *
     * @param channel the bundle channel to get the value of.
     * @return the current redstone output.
     */
    int getBundledRedstoneOutput(int channel);

    /**
     * Set the new bundled input value for the module.
     *
     * @param channel the bundled channel to set the value of.
     * @param value   the new input value of the module.
     */
    void setBundledRedstoneInput(int channel, int value);
}
