package li.cil.tis3d.common.network.message;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.common.tileentity.CasingTileEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

public final class ReceivingPipeLockedStateMessage extends AbstractMessageWithPosition {
    private Face face;
    private Port port;
    private boolean isLocked;

    public ReceivingPipeLockedStateMessage(final Casing casing, final Face face, final Port port, final boolean isLocked) {
        super(casing.getPosition());
        this.face = face;
        this.port = port;
        this.isLocked = isLocked;
    }

    @SuppressWarnings("unused") // For deserialization.
    public ReceivingPipeLockedStateMessage(final FriendlyByteBuf buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //

    @Override
    protected void handleMessage(final NetworkEvent.Context context) {
        final Level level = getClientLevel();
        if (level != null) {
            withTileEntity(level, CasingTileEntity.class, casing ->
                casing.setReceivingPipeLockedClient(face, port, isLocked));
        }
    }

    // We can nicely compress the data of this message into one byte:
    // - Face can have 6 values, needs 3 bits.
    // - Port can have 4 values, needs 2 bits.
    // - Locked can have 2 values, needs 1 bit.
    // It's an infrequent message, so this is totally overkill. But it's fun!

    @Override
    public void fromBytes(final FriendlyByteBuf buffer) {
        super.fromBytes(buffer);

        final byte compressed = buffer.readByte();
        face = Face.VALUES[(compressed >>> 3) & 0b111];
        port = Port.VALUES[(compressed >>> 1) & 0b11];
        isLocked = (compressed & 0b1) == 1;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buffer) {
        super.toBytes(buffer);

        final byte compressed = (byte) ((face.ordinal() << 3) |
                                        (port.ordinal() << 1) |
                                        (isLocked ? 1 : 0));
        buffer.writeByte(compressed);
    }
}
