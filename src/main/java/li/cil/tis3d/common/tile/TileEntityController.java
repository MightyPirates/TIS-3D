package li.cil.tis3d.common.tile;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * The controller tile entity.
 * <p>
 * Scans for multi-block structures if scheduled by either the controller
 * itself or by a connected casing. Manages a list of casings and updates
 * the modules in the casing (this is the only ticking part of a multi-block).
 * <p>
 * Controllers have no real state. They are active when powered by a redstone
 * signal, and can be reset by right-clicking them.
 */
public final class TileEntityController extends TileEntity implements ITickable {
    // --------------------------------------------------------------------- //
    // Computed data

    /**
     * The maximum number of casings that may be connected to a controller.
     */
    public static final int MAX_CASINGS = 16;

    /**
     * Possible states of a controller.
     */
    public enum ControllerState {
        /**
         * A scan has been scheduled and will be performed in the next tick.
         */
        SCANNING,

        /**
         * In the last scan another controller was found; only one is allowed per multi-block.
         */
        MULTIPLE_CONTROLLERS,

        /**
         * In the last scan more than {@link #MAX_CASINGS} casings were found.
         */
        TOO_COMPLEX,

        /**
         * In the last scan the border of the loaded area was hit; incomplete multi-blocks to nothing.
         */
        INCOMPLETE,

        /**
         * The controller is in operational state and will update connected casings each tick.
         */
        READY
    }

    /**
     * The list of casings managed by this controller.
     */
    private final List<TileEntityCasing> casings = new ArrayList<>(MAX_CASINGS);

    /**
     * The current state of the controller.
     */
    private ControllerState state = ControllerState.SCANNING;

    // --------------------------------------------------------------------- //

    /**
     * Get the current state of the controller.
     *
     * @return the current state of the controller.
     */
    public ControllerState getState() {
        return state;
    }

    /**
     * Schedule a rescan for connected casings.
     * <p>
     * If we're currently scanning this does nothing.
     */
    public void scheduleScan() {
        if (state != ControllerState.SCANNING) {
            clear(ControllerState.SCANNING);
        }
    }

    /**
     * Clear the list of controlled casings (and clear their controller), then
     * enter the specified state.
     *
     * @param toState the state to enter after clearing.
     */
    private void clear(final ControllerState toState) {
        for (final TileEntityCasing casing : casings) {
            casing.setController(null);
        }
        casings.clear();
        state = toState;
    }

    // --------------------------------------------------------------------- //
    // TileEntity

    @Override
    public void invalidate() {
        super.invalidate();
        notifyNeighbors();
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        notifyNeighbors();
    }

    private void notifyNeighbors() {
        for (final EnumFacing facing : EnumFacing.VALUES) {
            final BlockPos neighborPos = getPos().offset(facing);
            if (getWorld().isBlockLoaded(neighborPos)) {
                final TileEntity tileEntity = getWorld().getTileEntity(neighborPos);
                if (tileEntity instanceof TileEntityController) {
                    final TileEntityController controller = (TileEntityController) tileEntity;
                    controller.scheduleScan();
                } else if (tileEntity instanceof TileEntityCasing) {
                    final TileEntityCasing casing = (TileEntityCasing) tileEntity;
                    casing.scheduleScan();
                }
            }
        }
    }

    // --------------------------------------------------------------------- //
    // ITickable

    @Override
    public void update() {
        // Only update multi-block and casings on the server.
        if (getWorld().isRemote) {
            return;
        }

        // Do we have a redstone signal?
        if (!getWorld().isBlockPowered(getPos())) {
            return;
        }

        // Check if we need to rescan our multi-block structure.
        if (state == ControllerState.SCANNING) {
            scan();
        }

        // If we're in an error state we do nothing.
        if (state != ControllerState.READY) {
            return;
        }

        // All systems are go!
        casings.forEach(TileEntityCasing::stepModules);
        casings.forEach(TileEntityCasing::stepPipes);
    }

    // --------------------------------------------------------------------- //

    /**
     * Do a scan for connected casings starting from this controller.
     * <p>
     * Sets the state based on error or success and collects all found casings into
     * the {@link #casings} field on success.
     */
    private void scan() {
        // List of processed tile entities to avoid loops.
        final Set<TileEntity> processed = new HashSet<>();
        // List of pending tile entities that still need to be scanned.
        final Queue<TileEntity> queue = new ArrayDeque<>();

        // Start at our location, keep going until there's nothing left to do.
        processed.add(this);
        queue.add(this);
        while (!queue.isEmpty()) {
            final TileEntity tileEntity = queue.remove();

            // Check what we have. We only add controllers and casings to this list,
            // so we can skip the type check in the else branch.
            if (tileEntity instanceof TileEntityController) {
                if (tileEntity == this) {
                    // Special case: first iteration, add the neighbors.
                    if (!addNeighbors(tileEntity, processed, queue)) {
                        clear(ControllerState.INCOMPLETE);
                        return;
                    }
                } else {
                    // We require there to be exactly one controller per multi-block.
                    clear(ControllerState.MULTIPLE_CONTROLLERS);
                    return;
                }
            } else /* if (tileEntity instanceof TileEntityCasing) */ {
                // We only allow a certain number of casings per multi-block.
                if (casings.size() + 1 > MAX_CASINGS) {
                    clear(ControllerState.TOO_COMPLEX);
                    return;
                }

                // Register as the controller with the casing and add neighbors.
                final TileEntityCasing casing = (TileEntityCasing) tileEntity;
                casing.setController(this);
                casings.add(casing);
                addNeighbors(casing, processed, queue);
            }
        }

        // Ensure our casings know their neighbors.
        casings.forEach(TileEntityCasing::checkNeighbors);

        // Sort casings for deterministic order of execution (important when modules
        // write / read from multiple ports but only want to make the data available
        // to the first [e.g. execution module's ANY target]).
        casings.sort(CasingComparator.INSTANCE);

        // All done. Make sure this comes after the checkNeighbors or we get CMEs!
        state = ControllerState.READY;
    }

    private static class CasingComparator implements Comparator<TileEntityCasing> {
        public static final CasingComparator INSTANCE = new CasingComparator();

        @Override
        public int compare(final TileEntityCasing casing1, final TileEntityCasing casing2) {
            final BlockPos pos1 = casing1.getPos();
            final BlockPos pos2 = casing2.getPos();

            final int deltaY = pos2.getY() - pos1.getY();
            if (deltaY != 0) {
                return deltaY;
            }

            final int deltaZ = pos2.getZ() - pos1.getZ();
            if (deltaZ != 0) {
                return deltaZ;
            }

            final int deltaX = pos2.getX() - pos1.getX();
            if (deltaX != 0) {
                return deltaX;
            }

            return 0;
        }
    }

    /**
     * Checks all six neighbors of the specified tile entity and adds them to the
     * queue if they're a controller or casing and haven't been checked yet (or
     * added to the queue yet).
     * <p>
     * This returns a boolean value indicating whether a world border has been
     * hit. In this case we abort the search and wait, to avoid potentially
     * partially loaded multi-blocks.
     * <p>
     * Note that this is also used in {@link TileEntityCasing} for the reverse
     * search when trying to notify a controller.
     *
     * @param tileEntity the tile entity to get the neighbors for.
     * @param processed  the list of processed tile entities.
     * @param queue      the list of pending tile entities.
     * @return <tt>true</tt> if all neighbors could be checked, <tt>false</tt> otherwise.
     */
    static boolean addNeighbors(final TileEntity tileEntity, final Set<TileEntity> processed, final Queue<TileEntity> queue) {
        for (final EnumFacing facing : EnumFacing.VALUES) {
            final BlockPos neighborPos = tileEntity.getPos().offset(facing);
            if (!tileEntity.getWorld().isBlockLoaded(neighborPos)) {
                return false;
            }

            final TileEntity neighborTileEntity = tileEntity.getWorld().getTileEntity(neighborPos);
            if (neighborTileEntity == null) {
                continue;
            }
            if (!processed.add(neighborTileEntity)) {
                continue;
            }
            if (neighborTileEntity instanceof TileEntityController || neighborTileEntity instanceof TileEntityCasing) {
                queue.add(neighborTileEntity);
            }
        }
        return true;
    }
}
