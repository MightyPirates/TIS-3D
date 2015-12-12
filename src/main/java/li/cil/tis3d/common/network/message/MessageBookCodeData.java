package li.cil.tis3d.common.network.message;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import li.cil.tis3d.common.TIS3D;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

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
        try {
            final PacketBuffer buffer = new PacketBuffer(buf);
            nbt = buffer.readNBTTagCompoundFromBuffer();
        } catch (final IOException e) {
            TIS3D.getLog().warn("Invalid packet received.", e);
        }
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        try {
            final PacketBuffer buffer = new PacketBuffer(buf);
            buffer.writeNBTTagCompoundToBuffer(nbt);
        } catch (final IOException e) {
            TIS3D.getLog().warn("Failed sending packet.", e);
        }
    }
}
