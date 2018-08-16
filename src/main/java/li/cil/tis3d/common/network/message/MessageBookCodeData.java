package li.cil.tis3d.common.network.message;

import io.netty.buffer.ByteBuf;
import li.cil.tis3d.common.TIS3D;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import pl.asie.protocharset.rift.network.SendNetwork;

import java.io.IOException;

public final class MessageBookCodeData extends AbstractMessage {
    @SendNetwork public NBTTagCompound nbt;

    public MessageBookCodeData(final NBTTagCompound nbt) {
        this.nbt = nbt;
    }

    @SuppressWarnings("unused") // For deserialization.
    public MessageBookCodeData() {
    }

    // --------------------------------------------------------------------- //

    public NBTTagCompound getNbt() {
        return nbt;
    }

    // --------------------------------------------------------------------- //
    // IMessage
}
