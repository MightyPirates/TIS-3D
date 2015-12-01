package li.cil.tis3d.api;

import li.cil.tis3d.api.module.Module;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * A casing for TIS-3D modules.
 * <p>
 * This is implemented by the tile entity of TIS-3D casings.
 */
public interface Casing {
    /**
     * The world this casing resides in.
     *
     * @return the world the casing lives in.
     */
    World getWorld();

    /**
     * The position of the casing in the world it exists in.
     *
     * @return the position of the casing.
     */
    BlockPos getPosition();

    /**
     * Flag the casing as dirty so it is saved when the chunk containing it saved next.
     */
    void markDirty();

    /**
     * Get the module installed on the specified face of the casing.
     *
     * @param face the face to get the module for.
     * @return the module installed on that face, or <tt>null</tt>.
     */
    Module getModule(Face face);

    /**
     * Set the module for the specified face of the casing.
     * <p>
     * This is automatically called by the casing tile entity when items are
     * added or removed, you'll usually not need to call this directly.
     *
     * @param face   the face to install the module on.
     * @param module the module to install on the face, or <tt>null</tt> for none.
     */
    void setModule(Face face, Module module);

    /**
     * Get the input port on the specified side of a module in this casing.
     * <p>
     * There are two {@link Port}s between every pair of {@link Module}s
     * in a case. Specifically, each edge of a {@link Casing} has two
     * {@link Port}s, going into opposite directions. This method is used
     * to to get a {@link Port} based on its sink.
     *
     * @param face the face to get the port for.
     * @param side the side for which to get the port.
     * @return the input port on that side.
     */
    Port getInputPort(Face face, Side side);

    /**
     * Get the output port on the specified side of a module in this casing.
     * <p>
     * There are two {@link Port}s between every pair of {@link Module}s
     * in a case. Specifically, each edge of a {@link Casing} has two
     * {@link Port}s, going into opposite directions. This method is used
     * to to get a {@link Port} based on its source.
     *
     * @param face the face to get the port for.
     * @param side the side for which to get the port.
     * @return the output port on that side.
     */
    Port getOutputPort(Face face, Side side);

    /**
     * Calls {@link Module#step()} for all installed module and performs
     * additional casing specific logic as necessary.
     */
    void step();
}
