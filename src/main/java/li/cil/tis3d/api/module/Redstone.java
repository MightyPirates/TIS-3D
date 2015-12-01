package li.cil.tis3d.api.module;

/**
 * Modules implementing this will be queried by their {@link li.cil.tis3d.api.Casing}
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
