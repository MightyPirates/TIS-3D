package li.cil.tis3d.common.network.message;

import io.netty.buffer.ByteBuf;
import li.cil.tis3d.TIS3D;
import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;

public final class MessageModuleData extends AbstractMessageWithLocation {
    private Face face;
    private NBTTagCompound nbt;

    public MessageModuleData(final Casing casing, final Face face, final NBTTagCompound nbt) {
        super(casing.getCasingWorld(), casing.getPositionX(), casing.getPositionY(), casing.getPositionZ());
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
        try {
            super.fromBytes(buf);

            final PacketBuffer buffer = new PacketBuffer(buf);
            face = Face.valueOf(buffer.readStringFromBuffer(32));
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
            buffer.writeStringToBuffer(face.name());
            buffer.writeNBTTagCompoundToBuffer(nbt);
        } catch (final IOException e) {
            TIS3D.getLog().warn("Invalid packet received", e);
        }
    }
}
