package li.cil.tis3d.api.machine;

import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.ModuleBase;

/**
 * A casing for TIS-3D modules, now with 100% less Minecraft.
 */
public interface CasingBase {


    /**
     * Flag the casing as dirty so it is saved when the chunk containing it
     * saved next.
     */
    void markDirty();

    /**
     * Get the module installed on the specified face of the casing.
     *
     * @param face the face to get the module for.
     * @return the module installed on that face, or <tt>null</tt>.
     */
    ModuleBase getModule(Face face);

    /**
     * Get the receiving pipe on the specified port of a module in this casing.
     * <p>
     * There are two {@link Pipe}s between every pair of {@link Module}s
     * in a case. Specifically, each edge of a {@link Casing} has two
     * {@link Pipe}s, going into opposite directions. This method is used
     * to to get a {@link Pipe} based on its sink.
     *
     * @param face the face to get the port for.
     * @param port the port for which to get the port.
     * @return the input port on that port.
     */
    Pipe getReceivingPipe(Face face, Port port);

    /**
     * Get the sending pipe on the specified port of a module in this casing.
     * <p>
     * There are two {@link Pipe}s between every pair of {@link Module}s
     * in a case. Specifically, each edge of a {@link Casing} has two
     * {@link Pipe}s, going into opposite directions. This method is used
     * to to get a {@link Pipe} based on its source.
     *
     * @param face the face to get the port for.
     * @param port the port for which to get the port.
     * @return the output port on that port.
     */
    Pipe getSendingPipe(Face face, Port port);
}
