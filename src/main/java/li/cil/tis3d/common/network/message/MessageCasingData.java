package li.cil.tis3d.common.network.message;

import io.netty.buffer.ByteBuf;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.common.TIS3D;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;

public class MessageCasingData extends AbstractMessageWithLocation {
    private NBTTagCompound nbt;

    public MessageCasingData(final Casing casing, final NBTTagCompound nbt) {
        super(casing.getCasingWorld(), casing.getPositionX(), casing.getPositionY(), casing.getPositionZ());
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
        } catch (final IOException | IllegalArgumentException e) {
            TIS3D.getLog().warn("Invalid packet received.", e);
        }
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        try {
            super.toBytes(buf);

            final PacketBuffer buffer = new PacketBuffer(buf);
            buffer.writeNBTTagCompoundToBuffer(nbt);
        } catch (final IOException e) {
            TIS3D.getLog().warn("Failed sending packet", e);
        }
    }
}
