package li.cil.tis3d.common.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.io.IOException;

public final class MessageBookCodeData implements IMessage {
    private NBTTagCompound nbt;

    public MessageBookCodeData(final NBTTagCompound nbt) {
        this.nbt = nbt;
    }

    public MessageBookCodeData() {
    }

    // --------------------------------------------------------------------- //

    public NBTTagCompound getNbt() {
        return nbt;
    }

    // --------------------------------------------------------------------- //
    // IMessage

    @Override
    public void fromBytes(final ByteBuf buf) {
        final PacketBuffer buffer = new PacketBuffer(buf);
        try {
            nbt = buffer.readNBTTagCompoundFromBuffer();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        final PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeNBTTagCompoundToBuffer(nbt);
    }
}
