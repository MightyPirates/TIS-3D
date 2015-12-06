package li.cil.tis3d.common.network.message;

import io.netty.buffer.ByteBuf;
import li.cil.tis3d.api.Casing;

public final class MessageCasingState extends AbstractMessageWithLocation {
    private boolean isEnabled;

    public MessageCasingState(final Casing casing, final boolean isEnabled) {
        super(casing.getCasingWorld(), casing.getPositionX(), casing.getPositionY(), casing.getPositionZ());
        this.isEnabled = isEnabled;
    }

    public MessageCasingState() {
    }

    // --------------------------------------------------------------------- //

    public boolean isEnabled() {
        return isEnabled;
    }

    // --------------------------------------------------------------------- //
    // IMessage

    @Override
    public void fromBytes(final ByteBuf buf) {
        super.fromBytes(buf);

        isEnabled = buf.readBoolean();
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        super.toBytes(buf);

        buf.writeBoolean(isEnabled);
    }
}
