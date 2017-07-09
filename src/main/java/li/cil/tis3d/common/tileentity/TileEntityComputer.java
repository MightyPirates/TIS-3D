package li.cil.tis3d.common.tileentity;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.common.machine.PipeHost;
import li.cil.tis3d.common.machine.PipeImpl;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public abstract class TileEntityComputer extends TileEntity implements PipeHost {
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
                {Port.DOWN,  Port.DOWN, Port.DOWN,  Port.DOWN},   // Y_NEG
                {Port.UP,    Port.UP,   Port.UP,    Port.UP},     // Y_POS
                {Port.RIGHT, Port.LEFT, Port.DOWN,  Port.UP},     // Z_NEG
                {Port.RIGHT, Port.LEFT, Port.UP,    Port.DOWN},   // Z_POS
                {Port.RIGHT, Port.LEFT, Port.RIGHT, Port.RIGHT},  // X_NEG
                {Port.RIGHT, Port.LEFT, Port.LEFT,  Port.LEFT}    // X_POS
                //    LEFT        RIGHT       UP          DOWN
        };
    }

    // NBT tag names.
    private static final String TAG_PIPES = "pipes";

    protected final TileEntityComputer[] neighbors = new TileEntityComputer[Face.VALUES.length];
    protected final Forwarder[] forwarders = new Forwarder[Face.VALUES.length];

    // --------------------------------------------------------------------- //

    protected TileEntityComputer() {
        for (final Face face : Face.VALUES) {
            for (final Port port : Port.VALUES) {
                pipes[pack(face, port)] = new PipeImpl(this, face, mapFace(face, port), mapSide(face, port));
            }
        }
    }

    /**
     * Advances the logic of all pipes by calling {@link PipeImpl#step()} on them.
     * <p>
     * This will advance pipes with both an active read and write operation to
     * transferring mode, if they're not already in transferring mode.
     */
    public void stepPipes() {
        for (final PipeImpl pipe : pipes) {
            pipe.step();
        }
    }

    /**
     * Advances the virtual modules used to bridge edges between modules, calling
     * {@link Forwarder#step()} on them.
     */
    public void stepForwarders() {
        for (final Forwarder forwarder : forwarders) {
            if (forwarder != null) {
                forwarder.step();
            }
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
        return pipes[pack(face, port)];
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
        return pipes[packMapped(face, port)];
    }

    // --------------------------------------------------------------------- //
    // PipeHost

    @Override
    public World getPipeHostWorld() {
        return getWorldObj();
    }

    @Override
    public int getPipeHostPositionX() {
        return xCoord;
    }

    @Override
    public int getPipeHostPositionY() {
        return yCoord;
    }

    @Override
    public int getPipeHostPositionZ() {
        return zCoord;
    }

    @Override
    public void onWriteComplete(final Face sendingFace, final Port sendingPort) {
        final Forwarder forwarder = forwarders[sendingFace.ordinal()];
        if (forwarder != null) {
            forwarder.onWriteComplete(sendingPort);
        }
    }

    // --------------------------------------------------------------------- //
    // TileEntity

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        readFromNBTForServer(nbt);
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        writeToNBTForServer(nbt);
    }

    @Override
    public void onDataPacket(final NetworkManager manager, final S35PacketUpdateTileEntity packet) {
        readFromNBTForClient(packet.func_148857_g());
    }

    @Override
    public Packet getDescriptionPacket() {
        final NBTTagCompound nbt = new NBTTagCompound();
        writeToNBTForClient(nbt);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, nbt);
    }

    // --------------------------------------------------------------------- //

    public void checkNeighbors() {
        // When a neighbor changed, check all neighbors and register them in
        // our tile entity.
        for (final EnumFacing facing : EnumFacing.values()) {
            final int neighborX = xCoord + facing.getFrontOffsetX();
            final int neighborY = yCoord + facing.getFrontOffsetY();
            final int neighborZ = zCoord + facing.getFrontOffsetZ();
            if (getWorldObj().blockExists(neighborX, neighborY, neighborZ)) {
                // If we have a casing, set it as our neighbor.
                final TileEntity tileEntity = getWorldObj().getTileEntity(neighborX, neighborY, neighborZ);
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

    protected void setNeighbor(final Face face, final TileEntityComputer neighbor) {
        // If a neighbor changed, do a rescan in the controller.
        final TileEntityComputer oldNeighbor = neighbors[face.ordinal()];
        if (neighbor != oldNeighbor) {
            neighbors[face.ordinal()] = neighbor;
            scheduleScan();
        }

        // Adjust forwarders, connecting multiple casings.
        if (neighbor == null) {
            // No neighbor, remove the virtual connector module.
            forwarders[face.ordinal()] = null;
        } else if (forwarders[face.ordinal()] == null) {
            // Got a new connection, and we have not yet been set up by our
            // neighbor. Create a virtual module that will be responsible
            // for transferring data between the two casings.
            final Forwarder forwarder = new Forwarder(this, face);
            final Forwarder neighborForwarder = new Forwarder(neighbor, face.getOpposite());
            forwarder.setSink(neighborForwarder);
            neighborForwarder.setSink(forwarder);
            forwarders[face.ordinal()] = forwarder;
            neighbor.forwarders[face.getOpposite().ordinal()] = neighborForwarder;
        }
    }

    protected void readFromNBTForServer(final NBTTagCompound nbt) {
        final NBTTagList pipesNbt = nbt.getTagList(TAG_PIPES, Constants.NBT.TAG_COMPOUND);
        final int pipeCount = Math.min(pipesNbt.tagCount(), pipes.length);
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
            pipesNbt.appendTag(portNbt);
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
    private static Port mapSide(final Face face, final Port port) {
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
        return mapFace(face, port).ordinal() * Port.VALUES.length + mapSide(face, port).ordinal();
    }

    // --------------------------------------------------------------------- //

    /**
     * This is a "virtual module" for internal use, forwarding data on all incoming
     * ports to the linked sink forwarder. This is used to transfer data between
     * two adjacent casings. They're not actual modules since they are also present
     * in controllers (to allow forwarding around concave corners with the controller
     * in the corner), but are exclusively present with modules (i.e. there can't
     * be a module on a face if there's a forwarder and vice versa).
     * <p>
     * Forwarders are always created in pairs, and each takes care of one of the two
     * directions data has to be moved.
     */
    private static final class Forwarder {
        private final TileEntityComputer computer;
        private final Face face;
        private Forwarder other;

        private Forwarder(final TileEntityComputer computer, final Face face) {
            this.computer = computer;
            this.face = face;
        }

        public void setSink(final Forwarder other) {
            this.other = other;
        }

        public void step() {
            for (final Port port : Port.VALUES) {
                beginForwarding(port);
            }
        }

        public void onWriteComplete(final Port port) {
            beginForwarding(port);
        }

        // --------------------------------------------------------------------- //

        private void beginForwarding(final Port port) {
            final Pipe receivingPipe = computer.getReceivingPipe(face, port);

            final Pipe sendingPipe = other.computer.getSendingPipe(other.face, flipSide(face, port));
            if (sendingPipe.isReading() && !sendingPipe.isWriting()) {
                if (!receivingPipe.isReading()) {
                    receivingPipe.beginRead();
                }
                if (receivingPipe.canTransfer()) {
                    sendingPipe.beginWrite(receivingPipe.read());
                }
            } else if (receivingPipe.isReading()) {
                receivingPipe.cancelRead();
            }
        }

        private static Port flipSide(final Face face, final Port port) {
            if (face == Face.Y_NEG || face == Face.Y_POS) {
                return (port == Port.UP || port == Port.DOWN) ? port.getOpposite() : port;
            } else {
                return (port == Port.LEFT || port == Port.RIGHT) ? port.getOpposite() : port;
            }
        }
    }
}
