package li.cil.tis3d.common.network.message;

import io.netty.buffer.ByteBuf;
import li.cil.tis3d.common.TIS3D;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.io.IOException;

public final class MessageBookCodeData implements IMessage {
    private NBTTagCompound nbt;

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

    @Override
    public void fromBytes(final ByteBuf buf) {
        final PacketBuffer buffer = new PacketBuffer(buf);
        try {
            nbt = buffer.readCompoundTag();
        } catch (final IOException e) {
            TIS3D.getLog().warn("Invalid packet received.", e);
        }
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        final PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeCompoundTag(nbt);
    }
}
