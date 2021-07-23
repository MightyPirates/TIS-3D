package li.cil.tis3d.common.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;

public abstract class AbstractReadOnlyMemoryModuleDataMessage extends AbstractMessage {
    protected InteractionHand hand;
    protected byte[] data;

    protected AbstractReadOnlyMemoryModuleDataMessage(final InteractionHand hand, final byte[] data) {
        this.hand = hand;
        this.data = data;
    }

    protected AbstractReadOnlyMemoryModuleDataMessage(final FriendlyByteBuf buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    public void fromBytes(final FriendlyByteBuf buffer) {
        hand = buffer.readEnum(InteractionHand.class);
        data = new byte[buffer.readInt()];
        buffer.readBytes(data);
    }

    @Override
    public void toBytes(final FriendlyByteBuf buffer) {
        buffer.writeEnum(hand);
        buffer.writeInt(data.length);
        buffer.writeBytes(data);
    }
}
