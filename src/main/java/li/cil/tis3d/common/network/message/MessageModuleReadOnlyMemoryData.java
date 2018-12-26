package li.cil.tis3d.common.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.Hand;

public final class MessageModuleReadOnlyMemoryData extends AbstractMessage {
    private byte[] data;
    private Hand hand;

    public MessageModuleReadOnlyMemoryData(final byte[] data, final Hand hand) {
        this.data = data;
        this.hand = hand;
    }

    @SuppressWarnings("unused") // For deserialization.
    public MessageModuleReadOnlyMemoryData() {
    }

    // --------------------------------------------------------------------- //

    public byte[] getData() {
        return data;
    }

    public Hand getHand() {
        return hand;
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    public void fromBytes(final ByteBuf buf) {
        data = new byte[buf.readInt()];
        buf.readBytes(data);
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeInt(data.length);
        buf.writeBytes(data);
    }
}
