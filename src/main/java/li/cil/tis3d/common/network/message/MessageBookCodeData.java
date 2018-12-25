package li.cil.tis3d.common.network.message;

import li.cil.tis3d.charset.SendNetwork;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Hand;

public final class MessageBookCodeData extends AbstractMessage {
    @SendNetwork
    public CompoundTag nbt;
    @SendNetwork
    public Hand hand;

    public MessageBookCodeData(final CompoundTag nbt, final Hand hand) {
        this.nbt = nbt;
        this.hand = hand;
    }

    @SuppressWarnings("unused") // For deserialization.
    public MessageBookCodeData() {
    }

    // --------------------------------------------------------------------- //

    public CompoundTag getNbt() {
        return nbt;
    }

    public Hand getHand() {
        return hand;
    }

    // --------------------------------------------------------------------- //
    // IMessage
}
