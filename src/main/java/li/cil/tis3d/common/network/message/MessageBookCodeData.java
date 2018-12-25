package li.cil.tis3d.common.network.message;

import li.cil.tis3d.charset.SendNetwork;
import net.minecraft.nbt.CompoundTag;

public final class MessageBookCodeData extends AbstractMessage {
    @SendNetwork
    public CompoundTag nbt;

    public MessageBookCodeData(final CompoundTag nbt) {
        this.nbt = nbt;
    }

    @SuppressWarnings("unused") // For deserialization.
    public MessageBookCodeData() {
    }

    // --------------------------------------------------------------------- //

    public CompoundTag getNbt() {
        return nbt;
    }

    // --------------------------------------------------------------------- //
    // IMessage
}
