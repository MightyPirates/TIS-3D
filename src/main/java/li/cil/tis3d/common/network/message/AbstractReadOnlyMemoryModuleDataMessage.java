package li.cil.tis3d.common.network.message;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;

public abstract class AbstractReadOnlyMemoryModuleDataMessage extends AbstractMessage {
    protected Hand hand;
    protected byte[] data;

    protected AbstractReadOnlyMemoryModuleDataMessage(final Hand hand, final byte[] data) {
        this.hand = hand;
        this.data = data;
    }

    protected AbstractReadOnlyMemoryModuleDataMessage(final PacketBuffer buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    public void fromBytes(final PacketBuffer buffer) {
        hand = buffer.readEnumValue(Hand.class);
        data = new byte[buffer.readInt()];
        buffer.readBytes(data);
    }

    @Override
    public void toBytes(final PacketBuffer buffer) {
        buffer.writeEnumValue(hand);
        buffer.writeInt(data.length);
        buffer.writeBytes(data);
    }
}
