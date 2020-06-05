package li.cil.tis3d.common.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Hand;

public final class ReadOnlyMemoryModuleDataMessage extends AbstractMessage {
    private byte[] data;
    private Hand hand;

    public ReadOnlyMemoryModuleDataMessage(final byte[] data, final Hand hand) {
        this.data = data;
        this.hand = hand;
    }

    @SuppressWarnings("unused") // For deserialization.
    public ReadOnlyMemoryModuleDataMessage() {
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
        final PacketByteBuf buffer = new PacketByteBuf(buf);
        data = buffer.readByteArray();
        hand = buffer.readEnumConstant(Hand.class);
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        final PacketByteBuf buffer = new PacketByteBuf(buf);
        buffer.writeByteArray(data);
        buffer.writeEnumConstant(hand);
    }
}
