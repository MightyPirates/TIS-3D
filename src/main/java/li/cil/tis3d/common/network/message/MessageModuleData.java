package li.cil.tis3d.common.network.message;

import io.netty.buffer.ByteBuf;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;

public final class MessageModuleData extends AbstractMessageWithLocation {
    private Face face;
    private NBTTagCompound nbt;

    public MessageModuleData(final Casing casing, final Face face, final NBTTagCompound nbt) {
        super(casing.getCasingWorld(), casing.getPosition());
        this.face = face;
        this.nbt = nbt;
    }

    public MessageModuleData() {
    }

    // --------------------------------------------------------------------- //

    public Face getFace() {
        return face;
    }

    public NBTTagCompound getNbt() {
        return nbt;
    }

    // --------------------------------------------------------------------- //
    // IMessage

    @Override
    public void fromBytes(final ByteBuf buf) {
        super.fromBytes(buf);

        final PacketBuffer buffer = new PacketBuffer(buf);
        face = buffer.readEnumValue(Face.class);
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
        buffer.writeEnumValue(face);
        buffer.writeNBTTagCompoundToBuffer(nbt);
    }
}
