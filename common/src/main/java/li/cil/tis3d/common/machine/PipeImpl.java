package li.cil.tis3d.common.machine;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.util.EnumUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

/**
 * Implementation of {@link Pipe}s for passing data between {@link Module}s.
 */
public final class PipeImpl implements Pipe {
    // --------------------------------------------------------------------- //
    // Persisted data

    /**
     * The current state of the pipe.
     */
    private State readState = State.IDLE, writeState = State.IDLE;

    /**
     * The value currently being written over this pipe.
     */
    private short value = 0;

    // --------------------------------------------------------------------- //
    // Computed data

    /**
     * Current state of the pipe, to enforce synchronization, i.e. make sure
     * each read/write combination always takes the same amount of steps
     * regardless of whether reader or writer ran first (when they start in
     * the same step).
     */
    private enum State {
        /**
         * Waiting for a reader.
         */
        IDLE,

        /**
         * Reader registered in this update.
         */
        BUSY,

        /**
         * Reader has registered and is ready to receive.
         */
        READY,

        /**
         * Data can be read from the pipe this update.
         */
        FLUSHING,

        /**
         * Data was read from the pipe this update.
         */
        COMPLETE
    }

    // NBT tag names.
    private static final String TAG_READ_STATE = "readState";
    private static final String TAG_WRITE_STATE = "writeState";
    private static final String TAG_VALUE = "value";

    /**
     * The container this pipe belongs to.
     */
    private final PipeHost host;

    /**
     * The faces this pipe is connected to in the owning {@link Casing}.
     */
    private final Face receivingFace, sendingFace;

    /**
     * The input port of this pipe in the owning {@link Casing}.
     */
    private final Port sendingPort;

    // --------------------------------------------------------------------- //

    public PipeImpl(final PipeHost host, final Face receivingFace, final Face sendingFace, final Port sendingPort) {
        this.host = host;
        this.receivingFace = receivingFace;
        this.sendingFace = sendingFace;
        this.sendingPort = sendingPort;
    }

    /**
     * Called from the owning {@link Casing} after
     * all {@link Module}s have been updated to update the pipe's
     * state in a synchronized manner.
     */
    public void step() {
        if (writeState == State.BUSY) {
            writeState = State.READY;
        }
        if (readState == State.BUSY) {
            readState = State.READY;
        }
        if (writeState == State.READY && readState == State.READY) {
            writeState = State.FLUSHING;
            readState = State.FLUSHING;
        }
        if (writeState == State.COMPLETE && readState == State.COMPLETE) {
            finishTransfer();
        }

        host.onPipeStateChanged();
    }

    public void load(final CompoundTag tag) {
        readState = EnumUtils.load(State.class, TAG_READ_STATE, tag);
        writeState = EnumUtils.load(State.class, TAG_WRITE_STATE, tag);
        value = tag.getShort(TAG_VALUE);
    }

    public void save(final CompoundTag tag) {
        EnumUtils.save(readState, TAG_READ_STATE, tag);
        EnumUtils.save(writeState, TAG_WRITE_STATE, tag);
        tag.putShort(TAG_VALUE, value);
    }

    private void finishTransfer() {
        readState = State.IDLE;
        writeState = State.IDLE;
        value = 0;

        host.onWriteComplete(sendingFace, sendingPort);
    }

    // --------------------------------------------------------------------- //
    // Pipe

    @Override
    public void beginWrite(final short value) {
        if (writeState == State.COMPLETE) { // TODO Remove in MC 1.13
            return; // Silently ignore, backwards compatibility.
        }
        if (writeState != State.IDLE) {
            throw new IllegalStateException("Trying to write to a busy pipe. Check isWriting().");
        }
        writeState = State.BUSY;
        this.value = value;

        host.onPipeStateChanged();
    }

    @Override
    public void cancelWrite() {
        if (writeState == State.COMPLETE && readState == State.COMPLETE) {
            return; // Ignore, wait for next step() to avoid execution order dependent cycle count.
        }

        writeState = State.IDLE;
        value = 0;
        if (readState == State.FLUSHING) {
            readState = State.READY;
        }

        host.onPipeStateChanged();
    }

    @Override
    public boolean isWriting() {
        return writeState != State.IDLE;
    }

    @Override
    public void beginRead() {
        if (readState == State.COMPLETE) { // TODO Remove in MC 1.13
            return; // Silently ignore, backwards compatibility.
        }
        if (readState != State.IDLE) {
            throw new IllegalStateException("Trying to read from a busy pipe. Check isReading().");
        }
        readState = State.BUSY;

        host.onPipeStateChanged();
    }

    @Override
    public void cancelRead() {
        if (writeState == State.COMPLETE && readState == State.COMPLETE) {
            return; // Ignore, wait for next step() to avoid execution order dependent cycle count.
        }

        readState = State.IDLE;
        if (writeState == State.FLUSHING) {
            writeState = State.READY;
        }

        host.onPipeStateChanged();
    }

    @Override
    public boolean isReading() {
        return readState != State.IDLE;
    }

    @Override
    public boolean canTransfer() {
        return writeState == State.FLUSHING && readState == State.FLUSHING;
    }

    @Override
    public short read() {
        if (!canTransfer()) {
            throw new IllegalStateException("No data to read. Check canTransfer().");
        }

        writeState = State.COMPLETE;
        readState = State.COMPLETE;

        sendEffect();

        host.onBeforeWriteComplete(sendingFace, sendingPort);

        host.onPipeStateChanged();

        return value;
    }

    private void sendEffect() {
        final BlockPos position = host.getPipeHostPosition();
        final double ox = Face.toDirection(receivingFace).getStepX() + Face.toDirection(sendingFace).getStepX();
        final double oy = Face.toDirection(receivingFace).getStepY() + Face.toDirection(sendingFace).getStepY();
        final double oz = Face.toDirection(receivingFace).getStepZ() + Face.toDirection(sendingFace).getStepZ();
        final double x = ox * 0.55 + position.getX() + 0.5;
        final double y = oy * 0.55 + position.getY() + 0.5;
        final double z = oz * 0.55 + position.getZ() + 0.5;
        final double extraOffsetY = oy < 0 ? -0.2 : (oy > 0) ? 0.1 : 0;

        Network.sendPipeEffect(host.getPipeHostLevel(), x, y + extraOffsetY, z);
    }

    // --------------------------------------------------------------------- //
    // Object

    @Override
    public String toString() {
        return host.getPipeHostPosition() + ": " + sendingFace + " [" + writeState + "] -> " + receivingFace + " [" + readState + "]";
    }
}
