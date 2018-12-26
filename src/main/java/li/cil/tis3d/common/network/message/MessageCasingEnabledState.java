package li.cil.tis3d.common.network.message;

import io.netty.buffer.ByteBuf;
import li.cil.tis3d.api.machine.Casing;

public final class MessageCasingEnabledState extends AbstractMessageWithLocation {
    private boolean isEnabled;

    public MessageCasingEnabledState(final Casing casing, final boolean isEnabled) {
        super(casing.getCasingWorld(), casing.getPosition());
        this.isEnabled = isEnabled;
    }

    @SuppressWarnings("unused") // For deserialization.
    public MessageCasingEnabledState() {
    }

    // --------------------------------------------------------------------- //

    public boolean isEnabled() {
        return isEnabled;
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

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
