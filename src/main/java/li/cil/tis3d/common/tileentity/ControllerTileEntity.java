package li.cil.tis3d.common.tileentity;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.machine.HaltAndCatchFireException;
import li.cil.tis3d.common.CommonConfig;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.network.message.HaltAndCatchFireMessage;
import li.cil.tis3d.util.LevelUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;

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
public final class ControllerTileEntity extends ComputerTileEntity {
    // --------------------------------------------------------------------- //
    // Computed data

    /**
     * Time in ticks to wait before restarting execution after an HCF event.
     */
    private static final int COOLDOWN_HCF = 60;

    /**
     * Possible states of a controller.
     */
    public enum ControllerState {
        /**
         * A scan has been scheduled and will be performed in the next tick.
         */
        SCANNING(false),

        /**
         * In the last scan another controller was found; only one is allowed per multi-block.
         */
        MULTIPLE_CONTROLLERS(true),

        /**
         * In the last scan more than {@link CommonConfig#maxCasingsPerController} casings were found.
         */
        TOO_COMPLEX(true),

        /**
         * In the last scan the border of the loaded area was hit; incomplete multi-blocks to nothing.
         */
        INCOMPLETE(true),

        /**
         * The controller is in operational state and can update connected casings each tick.
         */
        READY(false),

        /**
         * The controller is in operational state and powered, updating connected casings each tick.
         */
        RUNNING(false);

        // --------------------------------------------------------------------- //

        /**
         * Whether this states is an error state, i.e. whether it indicates the controller
         * not operation normally due it being configured incorrectly, for example.
         */
        public final boolean isError;

        /**
         * The message to display for this status.
         */
        public final Component message;

        ControllerState(final boolean isError) {
            this.isError = isError;
            this.message = new TranslatableComponent(API.MOD_ID + ".controller.status." + name().toLowerCase(Locale.US));
        }

        // --------------------------------------------------------------------- //

        /**
         * All possible enum values for quick indexing.
         */
        public static final ControllerState[] VALUES = ControllerState.values();
    }

    /**
     * The list of casings managed by this controller.
     */
    private final List<CasingTileEntity> casings = new ArrayList<>(CommonConfig.maxCasingsPerController);

    /**
     * The current state of the controller.
     */
    private ControllerState state = ControllerState.SCANNING;

    /**
     * The last state we sent to clients, i.e. the state clients think the controller is in.
     */
    private ControllerState lastSentState = ControllerState.SCANNING;

    // NBT tag names.
    private static final String TAG_HCF_COOLDOWN = "hcfCooldown";
    private static final String TAG_STATE = "state";

    /**
     * User scheduled a forced step for the next tick.
     * <p>
     * This only matters if the machine is currently paused; in particular,
     * this is ignored if the machine is currently powered down. Therefore
     * there's not need to save the value, either.
     */
    private boolean forceStep;

    // --------------------------------------------------------------------- //
    // Persisted data

    /**
     * Time to keep waiting before resuming execution after an HCF event.
     */
    private int hcfCooldown = 0;

    // --------------------------------------------------------------------- //

    public ControllerTileEntity(final BlockPos pos, final BlockState state) {
        super(TileEntities.CONTROLLER.get(), pos, state);
    }

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
        state = ControllerState.SCANNING;
    }

    /**
     * If the controller is running, force at least one step in the next tick,
     * even if the controller is currently in the paused state. This will not
     * cause additional steps when not in the paused step!
     */
    public void forceStep() {
        final Level level = getBlockEntityLevel();
        if (state == ControllerState.RUNNING) {
            forceStep = true;
            final Vec3 pos = Vec3.atCenterOf(getBlockPos());
            level.playSound(null, pos.x(), pos.y(), pos.z(),
                SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.2f, 0.8f + level.random.nextFloat() * 0.1f);
        }
    }

    /**
     * Reset the controller, pause for a moment and catch fire.
     */
    public void haltAndCatchFire() {
        final Level level = getBlockEntityLevel();
        if (!level.isClientSide()) {
            state = ControllerState.READY;
            casings.forEach(CasingTileEntity::onDisabled);
            final HaltAndCatchFireMessage message = new HaltAndCatchFireMessage(getBlockPos());
            final PacketDistributor.PacketTarget target = Network.getTargetPoint(this, Network.RANGE_MEDIUM);
            Network.INSTANCE.send(target, message);
        }
        hcfCooldown = COOLDOWN_HCF;
    }

    // --------------------------------------------------------------------- //
    // TileEntity

    @Override
    public void setRemoved() {
        super.setRemoved();

        if (getBlockEntityLevel().isClientSide()) {
            return;
        }

        // If we were in an active state, deactivate all modules in connected cases.
        // Safe to always call this because the casings track their own enabled
        // state and just won't do anything if they're already disabled.
        // Also, this is guaranteed to be correct, because we were either the sole
        // controller, thus there is no controller anymore, or there were multiple
        // controllers, in which case they were disabled to start with.
        casings.forEach(CasingTileEntity::onDisabled);
        for (final CasingTileEntity casing : casings) {
            casing.setController(null);
        }
        casings.clear();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();

        // Just unset from our casings, do *not* disable them to keep their state.
        for (final CasingTileEntity casing : casings) {
            casing.setController(null);
        }
    }

    // --------------------------------------------------------------------- //
    // TileEntityComputer

    @Override
    protected void loadServer(final CompoundTag tag) {
        super.loadServer(tag);

        hcfCooldown = tag.getInt(TAG_HCF_COOLDOWN);
    }

    @Override
    protected void saveServer(final CompoundTag tag) {
        super.saveServer(tag);

        tag.putInt(TAG_HCF_COOLDOWN, hcfCooldown);
    }

    @Override
    protected void loadClient(final CompoundTag tag) {
        super.loadClient(tag);

        state = ControllerState.VALUES[tag.getByte(TAG_STATE) & 0xFF];
    }

    @Override
    protected void saveClient(final CompoundTag tag) {
        super.saveClient(tag);

        tag.putByte(TAG_STATE, (byte) state.ordinal());
    }

    // --------------------------------------------------------------------- //
    // Ticking

    public static void clientTick(final Level level, final BlockPos pos, final BlockState state, final ControllerTileEntity tileEntity) {
        tileEntity.clientTick();
    }

    public static void serverTick(final Level level, final BlockPos pos, final BlockState state, final ControllerTileEntity tileEntity) {
        tileEntity.serverTick();
    }

    private void clientTick() {
        final Level level = getBlockEntityLevel();

        if (hcfCooldown > 0) {
            --hcfCooldown;

            // TODO WTF is this, server on client check, huh??
            if (level instanceof final ServerLevel serverLevel) {
                // Spawn some fire particles! No actual fire, that'd be... problematic.
                for (final Direction facing : Direction.values()) {
                    final BlockPos neighborPos = getBlockPos().relative(facing);
                    final BlockState neighborState = level.getBlockState(neighborPos);
                    if (neighborState.isSolidRender(level, neighborPos)) {
                        continue;
                    }
                    if (level.random.nextFloat() > 0.25f) {
                        continue;
                    }
                    final float ox = neighborPos.getX() + level.random.nextFloat();
                    final float oy = neighborPos.getY() + level.random.nextFloat();
                    final float oz = neighborPos.getZ() + level.random.nextFloat();
                    serverLevel.sendParticles(ParticleTypes.FLAME, ox, oy, oz, 5, 0, 0, 0, 0);
                }
            }
        }
    }

    private void serverTick() {
        final Level level = getBlockEntityLevel();

        if (state != lastSentState) {
            final LevelChunk chunk = level.getChunkAt(getBlockPos());
            final BlockState blockState = level.getBlockState(getBlockPos());
            level.markAndNotifyBlock(getBlockPos(), chunk, blockState, blockState, 7, 512);
            lastSentState = state;
        }

        // Enforce cooldown after HCF event.
        if (hcfCooldown > 0) {
            --hcfCooldown;
            return;
        }

        // Check if we need to rescan our multi-block structure.
        if (state == ControllerState.SCANNING) {
            scan();
        }

        // Stop if we're in an invalid state.
        if (state != ControllerState.READY && state != ControllerState.RUNNING) {
            return;
        }

        // Get accumulated redstone power coming in.
        final int power = computePower();

        // If we're in an error state we do nothing.
        if (state == ControllerState.READY) {
            // Are we powered?
            if (power < 1) {
                // Nope, nothing to do then.
                return;
            } else {
                // Yes, switch to running state and enable modules.
                state = ControllerState.RUNNING;
                casings.forEach(CasingTileEntity::onEnabled);
            }
        }

        if (state == ControllerState.RUNNING) {
            // Ignore forceStep when not paused.
            forceStep = forceStep && power == 1;

            // Are we powered?
            if (!level.hasNeighborSignal(getBlockPos())) {
                // Nope, fall back to ready state, disable modules.
                state = ControllerState.READY;
                casings.forEach(CasingTileEntity::onDisabled);
            } else if (power > 1 || forceStep) {
                // Operating, step all casings redstone input info once.
                casings.forEach(CasingTileEntity::stepRedstone);

                try {
                    // 0 = off, we never have this or we'd be in the READY state.
                    // 1 = paused, i.e. we don't lose state, but don't step.
                    // [2-14] = step every 15-n-th step.
                    // 15 = step every tick.
                    // [16-75] = step n/15 times a tick.
                    // 75 = step 5 times a tick.
                    if (power < 15) {
                        // Stepping slower than 100%.
                        final int delay = 15 - power;
                        if (level.getGameTime() % delay == 0 || forceStep) {
                            step();
                        }
                    } else {
                        // Stepping faster than 100%.
                        final int steps = power / 15;
                        for (int step = 0; step < steps; step++) {
                            step();
                        }
                    }
                } catch (final HaltAndCatchFireException e) {
                    haltAndCatchFire();
                }
            }

            // Processed our forced step either way, reset flag.
            forceStep = false;
        }
    }

    // --------------------------------------------------------------------- //

    /**
     * Checks all six neighbors of the specified tile entity and adds them to the
     * queue if they're a controller or casing and haven't been checked yet (or
     * added to the queue yet).
     * <p>
     * This returns a boolean value indicating whether a level border has been
     * hit. In this case we abort the search and wait, to avoid potentially
     * partially loaded multi-blocks.
     * <p>
     * Note that this is also used in {@link CasingTileEntity} for the reverse
     * search when trying to notify a controller.
     * <p>
     * <em>Important</em>: we have to pass along a valid level object here
     * instead of relying on the passed tile entity's level, since we may
     * have caused tile entity creation in rare cases (e.g. broken saves
     * where tile entities were not restored during load), which will not have
     * their level set if this is called from the update loop (where newly
     * created tile entities are added to a separate list, and will be added
     * to their chunk and thus get their level set later on).
     *
     * @param level      the level we're scanning for tile entities in.
     * @param tileEntity the tile entity to get the neighbors for.
     * @param processed  the list of processed tile entities.
     * @param queue      the list of pending tile entities.
     * @return <tt>true</tt> if all neighbors could be checked, <tt>false</tt> otherwise.
     */
    static boolean addNeighbors(final Level level, final BlockEntity tileEntity, final Set<BlockEntity> processed, final Queue<BlockEntity> queue) {
        for (final Direction facing : Direction.values()) {
            final BlockPos neighborPos = tileEntity.getBlockPos().relative(facing);
            if (!LevelUtils.isLoaded(level, neighborPos)) {
                return false;
            }

            final BlockEntity neighborTileEntity = level.getBlockEntity(neighborPos);
            if (neighborTileEntity == null) {
                continue;
            }
            if (!processed.add(neighborTileEntity)) {
                continue;
            }
            if (neighborTileEntity instanceof ControllerTileEntity || neighborTileEntity instanceof CasingTileEntity) {
                queue.add(neighborTileEntity);
            }
        }
        return true;
    }

    /**
     * Do a scan for connected casings starting from this controller.
     * <p>
     * Sets the state based on error or success and collects all found casings into
     * the {@link #casings} field on success.
     */
    private void scan() {
        // Ensure our neighbors list is up-to-date. Need to do this first, so we
        // do this even if scans fail, otherwise we may fail to initialize a scan
        // when our neighbors change (e.g. duplicate controller removed).
        checkNeighbors();

        final Level level = getBlockEntityLevel();

        // List of processed tile entities to avoid loops.
        final Set<BlockEntity> processed = new HashSet<>();
        // List of pending tile entities that still need to be scanned.
        final Queue<BlockEntity> queue = new ArrayDeque<>();
        // List of new found casings.
        final List<CasingTileEntity> newCasings = new ArrayList<>(CommonConfig.maxCasingsPerController);

        // Start at our location, keep going until there's nothing left to do.
        processed.add(this);
        queue.add(this);
        while (!queue.isEmpty()) {
            final BlockEntity tileEntity = queue.remove();

            // Check what we have. We only add controllers and casings to this list,
            // so we can skip the type check in the else branch.
            if (tileEntity instanceof ControllerTileEntity) {
                if (tileEntity == this) {
                    // Special case: first iteration, add the neighbors.
                    if (!addNeighbors(level, tileEntity, processed, queue)) {
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
                if (newCasings.size() + 1 > CommonConfig.maxCasingsPerController) {
                    clear(ControllerState.TOO_COMPLEX);
                    return;
                }

                // Register as the controller with the casing and add neighbors.
                final CasingTileEntity casing = (CasingTileEntity) tileEntity;
                newCasings.add(casing);
                addNeighbors(level, casing, processed, queue);
            }
        }

        // Special handling in case we triggered tile entity creation while
        // scanning (see comment on addNeighbors), re-scan next tick when
        // they all have their level object set... but only exit after having
        // touched all of them, to make sure they've been created.
        if (newCasings.stream().anyMatch(c -> !c.hasLevel())) {
            return;
        }

        // Handle splits by first getting the set of casings we originally had
        // control over but no longer, setting their controller to null and
        // telling them to reschedule, just in case (onDisable *should* be fine
        // but better safe than sorry).
        casings.removeAll(newCasings);
        casings.forEach(c -> c.setController(null));
        casings.forEach(CasingTileEntity::scheduleScan);

        // Replace old list of casings with the new found ones, now that we're
        // sure we don't have to disable our old ones.
        casings.clear();
        casings.addAll(newCasings);
        casings.forEach(c -> c.setController(this));

        // Ensure our parts know their neighbors.
        casings.forEach(CasingTileEntity::checkNeighbors);
        casings.forEach(ComputerTileEntity::rebuildOverrides);
        rebuildOverrides();

        // Sort casings for deterministic order of execution (important when modules
        // write / read from multiple ports but only want to make the data available
        // to the first [e.g. execution module's ANY target]).
        casings.sort(Comparator.comparing(CasingTileEntity::getPosition));

        // All done. Make sure this comes after the checkNeighbors or we get CMEs!
        state = ControllerState.READY;
    }

    /**
     * Compute the <em>accumulative</em> redstone power applied to the controller.
     *
     * @return the accumulative redstone signal.
     */
    private int computePower() {
        final Level level = getBlockEntityLevel();

        int acc = 0;
        for (final Direction facing : Direction.values()) {
            acc += Math.max(0, Math.min(15, level.getSignal(getBlockPos().relative(facing), facing)));
        }
        return acc;
    }

    /**
     * Advance all computer parts by one step.
     */
    private void step() {
        casings.forEach(CasingTileEntity::stepModules);
        casings.forEach(CasingTileEntity::stepPipes);
        stepPipes();
    }

    /**
     * Clear the list of controlled casings (and clear their controller), then
     * enter the specified state.
     *
     * @param toState the state to enter after clearing.
     */
    private void clear(final ControllerState toState) {
        // Whatever we're clearing to, remove self from all casings first. If
        // we're clearing for a schedule that's fine because we'll find them
        // again (or won't, in which case we're unloading/partially unloaded
        // anyway), if we're disabling because of an error, that's also what
        // we want (because it could be the 'two controllers' error, in which
        // case we don't want to reference one of the controllers since that
        // could lead to confusion.
        for (final CasingTileEntity casing : casings) {
            casing.setController(null);
        }

        // Disable modules if we're in an errored state. If we're in an
        // incomplete state or rescanning, leave the state as is to avoid
        // unnecessarily resetting the computer.
        if (toState != ControllerState.INCOMPLETE) {
            casings.forEach(CasingTileEntity::onDisabled);
        }
        casings.clear();

        state = toState;
    }
}
