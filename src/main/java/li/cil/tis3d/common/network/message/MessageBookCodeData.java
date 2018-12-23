package li.cil.tis3d.common.network.message;

import io.netty.buffer.ByteBuf;
import li.cil.tis3d.common.TIS3D;
import net.minecraft.nbt.CompoundTag;
import li.cil.tis3d.charset.SendNetwork;

import java.io.IOException;

public final class MessageBookCodeData extends AbstractMessage {
    @SendNetwork public CompoundTag nbt;

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
