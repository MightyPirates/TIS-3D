package li.cil.tis3d.common.network.message;

import io.netty.buffer.ByteBuf;
import li.cil.tis3d.api.machine.Casing;

public class MessageCasingData extends AbstractMessageWithLocation {
    private ByteBuf data;

    public MessageCasingData(final Casing casing, final ByteBuf data) {
        super(casing.getCasingWorld(), casing.getPositionX(), casing.getPositionY(), casing.getPositionZ());
        this.data = data;
    }

    @SuppressWarnings("unused") // For deserialization.
    public MessageCasingData() {
    }

    // --------------------------------------------------------------------- //

    public ByteBuf getData() {
        return data;
    }

    // --------------------------------------------------------------------- //

    @Override
    public void fromBytes(final ByteBuf buf) {
        super.fromBytes(buf);

        final int count = buf.readInt();
        data = buf.readBytes(count);
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        super.toBytes(buf);

        buf.writeInt(data.readableBytes());
        buf.writeBytes(data);
    }
}
