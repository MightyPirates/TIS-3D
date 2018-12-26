package li.cil.tis3d.common.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Hand;
import net.minecraft.util.PacketByteBuf;

public final class CodeBookDataMessage extends AbstractMessage {
    private CompoundTag nbt;
    private Hand hand;

    public CodeBookDataMessage(final CompoundTag nbt, final Hand hand) {
        this.nbt = nbt;
        this.hand = hand;
    }

    @SuppressWarnings("unused") // For deserialization.
    public CodeBookDataMessage() {
    }

    // --------------------------------------------------------------------- //

    public CompoundTag getNbt() {
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
        nbt = buffer.readCompoundTag();
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        final PacketByteBuf buffer = new PacketByteBuf(buf);
        buffer.writeCompoundTag(nbt);
    }
}
