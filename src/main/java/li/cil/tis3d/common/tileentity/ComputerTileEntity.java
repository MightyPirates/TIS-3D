package li.cil.tis3d.common.tileentity;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.common.machine.PipeHost;
import li.cil.tis3d.common.machine.PipeImpl;
import li.cil.tis3d.util.LevelUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Objects;

public abstract class ComputerTileEntity extends BlockEntity implements PipeHost {
    // --------------------------------------------------------------------- //
    // Persisted data.

    /**
     * The flat list of all {@link Pipe}s on this casing.
     * <p>
     * Indexed by face and port using {@link #pack(Face, Port)}.
     */
    private final PipeImpl[] pipes = new PipeImpl[Face.VALUES.length * Port.VALUES.length];

    // --------------------------------------------------------------------- //
    // Computed data.

    // Mapping for faces and ports around edges, i.e. to get the other side
    // of an edge specified by a face and port.
    private static final Face[][] FACE_MAPPING;
    private static final Port[][] PORT_MAPPING;

    static {
        FACE_MAPPING = new Face[][]{
            {Face.X_POS, Face.X_NEG, Face.Z_NEG, Face.Z_POS}, // Y_NEG
            {Face.X_POS, Face.X_NEG, Face.Z_POS, Face.Z_NEG}, // Y_POS
            {Face.X_POS, Face.X_NEG, Face.Y_POS, Face.Y_NEG}, // Z_NEG
            {Face.X_NEG, Face.X_POS, Face.Y_POS, Face.Y_NEG}, // Z_POS
            {Face.Z_NEG, Face.Z_POS, Face.Y_POS, Face.Y_NEG}, // X_NEG
            {Face.Z_POS, Face.Z_NEG, Face.Y_POS, Face.Y_NEG}  // X_POS
            //    LEFT        RIGHT       UP          DOWN
        };
        PORT_MAPPING = new Port[][]{
            {Port.DOWN, Port.DOWN, Port.DOWN, Port.DOWN},     // Y_NEG
            {Port.UP, Port.UP, Port.UP, Port.UP},             // Y_POS
            {Port.RIGHT, Port.LEFT, Port.DOWN, Port.UP},      // Z_NEG
            {Port.RIGHT, Port.LEFT, Port.UP, Port.DOWN},      // Z_POS
            {Port.RIGHT, Port.LEFT, Port.RIGHT, Port.RIGHT},  // X_NEG
            {Port.RIGHT, Port.LEFT, Port.LEFT, Port.LEFT}     // X_POS
            //    LEFT        RIGHT       UP          DOWN
        };
    }

    // NBT tag names.
    private static final String TAG_PIPES = "pipes";

    private final ComputerTileEntity[] neighbors = new ComputerTileEntity[Face.VALUES.length];
    private final PipeImpl[] pipeOverride = new PipeImpl[pipes.length];

    // --------------------------------------------------------------------- //

    protected ComputerTileEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);

        for (final Face face : Face.VALUES) {
            for (final Port port : Port.VALUES) {
                final int pipeIndex = pack(face, port);
                pipeOverride[pipeIndex] = pipes[pipeIndex] = new PipeImpl(this, face, mapFace(face, port), mapPort(face, port));
            }
        }
    }

    public Level getBlockEntityLevel() {
        return Objects.requireNonNull(getLevel());
    }

    /**
     * Advances the logic of all pipes by calling {@link PipeImpl#step()} on them.
     * <p>
     * This will advance pipes with both an active read and write operation to
     * transferring mode, if they're not already in transferring mode.
     */
    void stepPipes() {
        for (final PipeImpl pipe : pipes) {
            pipe.step();
        }
    }

    /**
     * Get the list of all pipes managed by this computer part.
     *
     * @return the list of pipes.
     */
    public Pipe[] getPipes() {
        return pipes;
    }

    /**
     * Receiving pipe for the specified face and port.
     *
     * @param face the face to get the port for.
     * @param port the port for which to get the port.
     * @return the input port on that port.
     * @see li.cil.tis3d.api.machine.Casing#getReceivingPipe(Face, Port)
     */
    public Pipe getReceivingPipe(final Face face, final Port port) {
        return pipeOverride[pack(face, port)];
    }

    /**
     * Sending pipe for the specified face and port.
     *
     * @param face the face to get the port for.
     * @param port the port for which to get the port.
     * @return the output port on that port.
     * @see li.cil.tis3d.api.machine.Casing#getSendingPipe(Face, Port)
     */
    public Pipe getSendingPipe(final Face face, final Port port) {
        return pipeOverride[packMapped(face, port)];
    }

    // --------------------------------------------------------------------- //
    // PipeHost

    @Override
    public Level getPipeHostLevel() {
        return getBlockEntityLevel();
    }

    @Override
    public BlockPos getPipeHostPosition() {
        return getBlockPos();
    }

    // --------------------------------------------------------------------- //
    // TileEntity

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        loadServer(tag);
    }

    @Override
    public CompoundTag save(final CompoundTag compound) {
        final CompoundTag tag = super.save(compound);
        saveServer(tag);
        return tag;
    }

    @Override
    public void onDataPacket(final Connection manager, final ClientboundBlockEntityDataPacket packet) {
        loadClient(packet.getTag());
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        final CompoundTag tag = new CompoundTag();
        saveClient(tag);
        return new ClientboundBlockEntityDataPacket(getBlockPos(), 0, tag);
    }

    @Override
    public void handleUpdateTag(final CompoundTag tag) {
        super.handleUpdateTag(tag);
        loadClient(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        final CompoundTag tag = super.getUpdateTag();
        saveClient(tag);
        return tag;
    }

    // --------------------------------------------------------------------- //

    public void checkNeighbors() {
        final Level level = getBlockEntityLevel();

        // When a neighbor changed, check all neighbors and register them in
        // our tile entity.
        for (final Direction facing : Direction.values()) {
            final BlockPos neighborPos = getBlockPos().relative(facing);
            if (LevelUtils.isLoaded(level, neighborPos)) {
                // If we have a casing, set it as our neighbor.
                final BlockEntity tileEntity = level.getBlockEntity(neighborPos);
                if (tileEntity instanceof ComputerTileEntity) {
                    setNeighbor(Face.fromDirection(facing), (ComputerTileEntity) tileEntity);
                } else {
                    setNeighbor(Face.fromDirection(facing), null);
                }
            } else {
                // Neighbor is in unloaded area.
                setNeighbor(Face.fromDirection(facing), null);
            }
        }
    }

    protected abstract void scheduleScan();

    protected void setNeighbor(final Face face, @Nullable final ComputerTileEntity neighbor) {
        // If a neighbor changed, do a rescan in the controller.
        final ComputerTileEntity oldNeighbor = neighbors[face.ordinal()];
        if (neighbor != oldNeighbor) {
            neighbors[face.ordinal()] = neighbor;
            scheduleScan();
        }
    }

    protected void loadServer(final CompoundTag tag) {
        final ListTag pipesTag = tag.getList(TAG_PIPES, Constants.NBT.TAG_COMPOUND);
        final int pipeCount = Math.min(pipesTag.size(), pipes.length);
        for (int i = 0; i < pipeCount; i++) {
            pipes[i].load(pipesTag.getCompound(i));
        }

        loadCommon(tag);
    }

    protected void saveServer(final CompoundTag tag) {
        final ListTag pipesTag = new ListTag();
        for (final PipeImpl pipe : pipes) {
            final CompoundTag portTag = new CompoundTag();
            pipe.save(portTag);
            pipesTag.add(portTag);
        }
        tag.put(TAG_PIPES, pipesTag);

        saveCommon(tag);
    }

    protected void loadClient(final CompoundTag tag) {
        loadCommon(tag);
    }

    protected void saveClient(final CompoundTag tag) {
        saveCommon(tag);
    }

    protected void loadCommon(final CompoundTag tag) {
    }

    protected void saveCommon(final CompoundTag tag) {
    }

    boolean hasNeighbor(final Face face) {
        return neighbors[face.ordinal()] != null;
    }

    void rebuildOverrides() {
        // Reset to initial state before checking for inter-block connections.
        System.arraycopy(pipes, 0, pipeOverride, 0, pipes.length);

        // Check each open face's neighbors, if they're in front of another
        // computer block, start connecting the pipe to where that leads us.
        for (final Face face : Face.VALUES) {
            if (neighbors[face.ordinal()] != null) {
                continue;
            }

            for (final Port port : Port.VALUES) {
                final Face otherFace = mapFace(face, port);
                final Port otherPort = mapPort(face, port);

                final ComputerTileEntity neighbor = neighbors[otherFace.ordinal()];
                if (neighbor != null) {
                    final Face neighborFace = otherFace.getOpposite();
                    final Port neighborPort = flipSide(otherFace, otherPort);
                    neighbor.computePipeOverrides(neighborFace, neighborPort, this, face, port);
                }
            }
        }
    }

    // --------------------------------------------------------------------- //

    /**
     * Get the the face on the other side of an edge.
     *
     * @param face the face defining the edge.
     * @param port the port defining the edge.
     * @return the face on the other side of the edge.
     */
    private static Face mapFace(final Face face, final Port port) {
        return FACE_MAPPING[face.ordinal()][port.ordinal()];
    }

    /**
     * Get the the port on the other side of an edge, relative to the face on
     * the other side of the edge.
     *
     * @param face the face defining the edge.
     * @param port the port defining the edge.
     * @return the port on the other side of the edge.
     */
    private static Port mapPort(final Face face, final Port port) {
        return PORT_MAPPING[face.ordinal()][port.ordinal()];
    }

    /**
     * Convert a face-port tuple to a unique number.
     *
     * @param face the face to pack into the number.
     * @param port the port to pack into the number.
     * @return the compressed representation of the face-port tuple.
     */
    private static int pack(final Face face, final Port port) {
        return face.ordinal() * Port.VALUES.length + port.ordinal();
    }

    /**
     * Map a face-port tuple to the face-tuple representing its opposite (i.e.
     * the face-port tuple defining the same edge but from the other side),
     * then convert it to a unique number.
     *
     * @param face the face defining the edge to the face to pack.
     * @param port the port defining the edge to the port to pack.
     * @return the compressed representation of the mapped face-port tuple.
     */
    private static int packMapped(final Face face, final Port port) {
        return mapFace(face, port).ordinal() * Port.VALUES.length + mapPort(face, port).ordinal();
    }

    /**
     * Get the port opposite to the specified port in a casing opposite to the
     * the specified facing. Used when connecting across multiple casings.
     *
     * @param face the face opposite to which to get the port for.
     * @param port the port opposite to which to get the port for.
     * @return the port opposite to the specified port on the specified face.
     */
    private static Port flipSide(final Face face, final Port port) {
        if (face == Face.Y_NEG || face == Face.Y_POS) {
            return (port == Port.UP || port == Port.DOWN) ? port.getOpposite() : port;
        } else {
            return (port == Port.LEFT || port == Port.RIGHT) ? port.getOpposite() : port;
        }
    }

    /**
     * Populates the {@link #pipeOverride} array for the specified computer's
     * face and port by traversing the computer multi-block until an open face
     * is found that at face and port connects to. Used to bridge casings so
     * that we can write values to modules of other casings without latency.
     *
     * @param face      the face of <em>this</em> casing to search from.
     * @param port      the port of <em>this</em> casing to search from.
     * @param start     the computer we're searching for.
     * @param startFace the face on the computer we're searching for.
     * @param startPort the port on the computer we're searching for.
     */
    private void computePipeOverrides(final Face face, final Port port, final ComputerTileEntity start, final Face startFace, final Port startPort) {
        // Avoid cycles for inner faces of 2x2 structures.
        if (start == this) {
            return;
        }

        final Face otherFace = mapFace(face, port);
        final Port otherPort = mapPort(face, port);

        final ComputerTileEntity neighbor = neighbors[otherFace.ordinal()];
        if (neighbor != null) {
            // Got a neighbor, continue searching through it. This can continue
            // only two times before we run into the early exit above.
            final Face neighborFace = otherFace.getOpposite();
            final Port neighborPort = flipSide(otherFace, otherPort);
            neighbor.computePipeOverrides(neighborFace, neighborPort, start, startFace, startPort);
        } else {
            // No neighbor, we have an open face. Use as target for the pipe.
            // override in the original computer. Setting this up in one
            // direction suffices as this is performed in both directions.
            final int receivingIndex = pack(startFace, startPort);
            final int mySendingIndex = packMapped(otherFace, otherPort);
            start.pipeOverride[receivingIndex] = pipes[mySendingIndex];
        }
    }
}
