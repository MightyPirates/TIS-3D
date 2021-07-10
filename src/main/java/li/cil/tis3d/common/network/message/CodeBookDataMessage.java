package li.cil.tis3d.common.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Hand;

public final class CodeBookDataMessage extends AbstractMessage {
    private NbtCompound nbt;
    private Hand hand;

    public CodeBookDataMessage(final NbtCompound nbt, final Hand hand) {
        this.nbt = nbt;
        this.hand = hand;
    }

    @SuppressWarnings("unused") // For deserialization.
    public CodeBookDataMessage() {
    }

    // --------------------------------------------------------------------- //

    public NbtCompound getNbt() {
        return nbt;
    }

    public Hand getHand() {
        return hand;
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    public void fromBytes(final ByteBuf buf) {
        final PacketByteBuf buffer = new PacketByteBuf(buf);
        nbt = buffer.readNbt();
        hand = buffer.readEnumConstant(Hand.class);
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        final PacketByteBuf buffer = new PacketByteBuf(buf);
        buffer.writeNbt(nbt);
        buffer.writeEnumConstant(hand);
    }
}
