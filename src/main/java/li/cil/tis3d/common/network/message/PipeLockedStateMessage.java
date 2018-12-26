package li.cil.tis3d.common.network.message;

import io.netty.buffer.ByteBuf;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;

public final class PipeLockedStateMessage extends AbstractMessageWithLocation {
    public Face face;
    public Port port;
    public boolean isLocked;

    public PipeLockedStateMessage(final CasingBlockEntity casing, final Face face, final Port port, final boolean isLocked) {
        super(casing.getWorld(), casing.getPos());
        this.face = face;
        this.port = port;
        this.isLocked = isLocked;
    }

    @SuppressWarnings("unused") // For deserialization.
    public PipeLockedStateMessage() {
    }

    // --------------------------------------------------------------------- //

    public Face getFace() {
        return face;
    }

    public Port getPort() {
        return port;
    }

    public boolean isLocked() {
        return isLocked;
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    // We can nicely compress the data of this message into one byte:
    // - Face can have 6 values, needs 3 bits.
    // - Port can have 4 values, needs 2 bits.
    // - Locked can have 2 values, needs 1 bit.
    // It's an infrequent message, so this is totally overkill. But it's fun!

    @Override
    public void fromBytes(final ByteBuf buf) {
        super.fromBytes(buf);

        final byte compressed = buf.readByte();
        face = Face.VALUES[(compressed >>> 3) & 0b111];
        port = Port.VALUES[(compressed >>> 1) & 0b11];
        isLocked = (compressed & 0b1) == 1;
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        super.toBytes(buf);

        final byte compressed = (byte) ((face.ordinal() << 3) | (port.ordinal() << 1) | (isLocked ? 1 : 0));
        buf.writeByte(compressed);
    }
}
