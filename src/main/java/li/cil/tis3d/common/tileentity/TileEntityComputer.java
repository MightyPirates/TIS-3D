package li.cil.tis3d.common.tileentity;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.machine.PipeHost;
import li.cil.tis3d.common.machine.PipeImpl;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

abstract class TileEntityComputer extends TileEntity implements PipeHost {
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
                {Port.DOWN,  Port.DOWN,  Port.DOWN,  Port.DOWN},   // Y_NEG
                {Port.UP,    Port.UP,    Port.UP,    Port.UP},     // Y_POS
                {Port.RIGHT, Port.LEFT,  Port.DOWN,  Port.UP},     // Z_NEG
                {Port.RIGHT, Port.LEFT,  Port.UP,    Port.DOWN},   // Z_POS
                {Port.RIGHT, Port.LEFT,  Port.RIGHT, Port.RIGHT},  // X_NEG
                {Port.RIGHT, Port.LEFT,  Port.LEFT,  Port.LEFT}    // X_POS
                //    LEFT        RIGHT       UP          DOWN
        };
    }

    // NBT tag names.
    private static final String TAG_PIPES = "pipes";

    private final TileEntityComputer[] neighbors = new TileEntityComputer[Face.VALUES.length];
    private final PipeImpl[] pipeOverride = new PipeImpl[pipes.length];

    // --------------------------------------------------------------------- //

    TileEntityComputer(TileEntityType type) {
    	super(type);
        for (final Face face : Face.VALUES) {
            for (final Port port : Port.VALUES) {
                final int pipeIndex = pack(face, port);
                pipeOverride[pipeIndex] = pipes[pipeIndex] = new PipeImpl(this, face, mapFace(face, port), mapPort(face, port));
            }
        }
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
    public World getPipeHostWorld() {
        return getWorld();
    }

    @Override
    public BlockPos getPipeHostPosition() {
        return getPos();
    }

    @Override
    public void onWriteComplete(final Face sendingFace, final Port sendingPort) {
    }

    // --------------------------------------------------------------------- //
    // TileEntity

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (nbt.hasKey("_client")) {
        	readFromNBTForClient(nbt);
        } else {
	        readFromNBTForServer(nbt);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbtIn) {
        final NBTTagCompound nbt = super.writeToNBT(nbtIn);
        writeToNBTForServer(nbt);
        return nbt;
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        final NBTTagCompound nbt = new NBTTagCompound();
        writeToNBTForClient(nbt);
        nbt.setBoolean("_client", true);
        return new SPacketUpdateTileEntity(getPos(), 0, nbt);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        final NBTTagCompound nbt = super.getUpdateTag();
        writeToNBTForClient(nbt);
        return nbt;
    }

    // --------------------------------------------------------------------- //

    public void checkNeighbors() {
        // When a neighbor changed, check all neighbors and register them in
        // our tile entity.
        for (final EnumFacing facing : EnumFacing.values()) {
            final BlockPos neighborPos = getPos().offset(facing);
            if (getWorld().isBlockLoaded(neighborPos)) {
                // If we have a casing, set it as our neighbor.
                final TileEntity tileEntity = getWorld().getTileEntity(neighborPos);
                if (tileEntity instanceof TileEntityComputer) {
                    setNeighbor(Face.fromEnumFacing(facing), (TileEntityComputer) tileEntity);
                } else {
                    setNeighbor(Face.fromEnumFacing(facing), null);
                }
            } else {
                // Neighbor is in unloaded area.
                setNeighbor(Face.fromEnumFacing(facing), null);
            }
        }
    }

    protected abstract void scheduleScan();

    protected void setNeighbor(final Face face, @Nullable final TileEntityComputer neighbor) {
        // If a neighbor changed, do a rescan in the controller.
        final TileEntityComputer oldNeighbor = neighbors[face.ordinal()];
        if (neighbor != oldNeighbor) {
            neighbors[face.ordinal()] = neighbor;
            scheduleScan();
        }
    }

    protected void readFromNBTForServer(final NBTTagCompound nbt) {
        final NBTTagList pipesNbt = nbt.getTagList(TAG_PIPES, Constants.NBT.TAG_COMPOUND);
        final int pipeCount = Math.min(pipesNbt.size(), pipes.length);
        for (int i = 0; i < pipeCount; i++) {
            pipes[i].readFromNBT(pipesNbt.getCompoundTagAt(i));
        }

        readFromNBTCommon(nbt);
    }

    protected void writeToNBTForServer(final NBTTagCompound nbt) {
        final NBTTagList pipesNbt = new NBTTagList();
        for (final PipeImpl pipe : pipes) {
            final NBTTagCompound portNbt = new NBTTagCompound();
            pipe.writeToNBT(portNbt);
            pipesNbt.add(portNbt);
        }
        nbt.setTag(TAG_PIPES, pipesNbt);

        writeToNBTCommon(nbt);
    }

    protected void readFromNBTForClient(final NBTTagCompound nbt) {
        readFromNBTCommon(nbt);
    }

    protected void writeToNBTForClient(final NBTTagCompound nbt) {
        writeToNBTCommon(nbt);
    }

    protected void readFromNBTCommon(final NBTTagCompound nbt) {
    }

    protected void writeToNBTCommon(final NBTTagCompound nbt) {
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

                final TileEntityComputer neighbor = neighbors[otherFace.ordinal()];
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
    private void computePipeOverrides(final Face face, final Port port, final TileEntityComputer start, final Face startFace, final Port startPort) {
        // Avoid cycles for inner faces of 2x2 structures.
        if (start == this) {
            return;
        }

        final Face otherFace = mapFace(face, port);
        final Port otherPort = mapPort(face, port);

        final TileEntityComputer neighbor = neighbors[otherFace.ordinal()];
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
