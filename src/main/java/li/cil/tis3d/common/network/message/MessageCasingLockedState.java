package li.cil.tis3d.common.network.message;

import io.netty.buffer.ByteBuf;
import li.cil.tis3d.api.machine.Casing;

public final class MessageCasingLockedState extends AbstractMessageWithLocation {
    private boolean isLocked;

    public MessageCasingLockedState(final Casing casing, final boolean isLocked) {
        super(casing.getCasingWorld(), casing.getPositionX(), casing.getPositionY(), casing.getPositionZ());
        this.isLocked = isLocked;
    }

    @SuppressWarnings("unused") // For deserialization.
    public MessageCasingLockedState() {
    }

    // --------------------------------------------------------------------- //

    public boolean isLocked() {
        return isLocked;
    }

    // --------------------------------------------------------------------- //
    // IMessage

    @Override
    public void fromBytes(final ByteBuf buf) {
        super.fromBytes(buf);

        isLocked = buf.readBoolean();
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        super.toBytes(buf);

        buf.writeBoolean(isLocked);
    }
}
