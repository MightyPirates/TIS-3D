package li.cil.tis3d.common.network.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import li.cil.tis3d.api.machine.Casing;
import pl.asie.protocharset.rift.network.SendNetwork;

public final class MessageCasingData extends AbstractMessageWithLocation {
    // TODO
    @SendNetwork public byte[] data;

    public MessageCasingData(final Casing casing, final ByteBuf data) {
        super(casing.getCasingWorld(), casing.getPosition());
        this.data = new byte[data.readableBytes()];
        data.readBytes(this.data);
    }

    @SuppressWarnings("unused") // For deserialization.
    public MessageCasingData() {
    }

    // --------------------------------------------------------------------- //

    public ByteBuf getData() {
        return Unpooled.wrappedBuffer(data);
    }

    // --------------------------------------------------------------------- //
}
