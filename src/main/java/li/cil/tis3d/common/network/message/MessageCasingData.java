package li.cil.tis3d.common.network.message;

import io.netty.buffer.ByteBuf;
import li.cil.tis3d.api.machine.Casing;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;

public class MessageCasingData extends AbstractMessageWithLocation {
    private NBTTagCompound nbt;

    public MessageCasingData(final Casing casing, final NBTTagCompound nbt) {
        super(casing.getCasingWorld(), casing.getPosition());
        this.nbt = nbt;
    }

    public MessageCasingData() {
    }

    // --------------------------------------------------------------------- //

    public NBTTagCompound getNbt() {
        return nbt;
    }

    // --------------------------------------------------------------------- //


    @Override
    public void fromBytes(final ByteBuf buf) {
        super.fromBytes(buf);

        final PacketBuffer buffer = new PacketBuffer(buf);
        try {
            nbt = buffer.readNBTTagCompoundFromBuffer();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        super.toBytes(buf);

        final PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeNBTTagCompoundToBuffer(nbt);
    }
}
