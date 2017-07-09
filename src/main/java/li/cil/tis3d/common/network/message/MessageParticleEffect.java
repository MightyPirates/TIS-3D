package li.cil.tis3d.common.network.message;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import li.cil.tis3d.common.TIS3D;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;

import java.io.IOException;

public final class MessageParticleEffect implements IMessage {
    private int dimension;
    private String particleType;
    private double x;
    private double y;
    private double z;

    public MessageParticleEffect(final World world, final String particleType, final double x, final double y, final double z) {
        this.dimension = world.provider.dimensionId;
        this.particleType = particleType;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public MessageParticleEffect() {
    }

    // --------------------------------------------------------------------- //

    public int getDimension() {
        return dimension;
    }

    public String getParticleType() {
        return particleType;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    // --------------------------------------------------------------------- //
    // IMessage

    @Override
    public void fromBytes(final ByteBuf buf) {
        final PacketBuffer buffer = new PacketBuffer(buf);
        try {
            dimension = buffer.readInt();
            particleType = buffer.readStringFromBuffer(32);
            x = buffer.readDouble();
            y = buffer.readDouble();
            z = buffer.readDouble();
        } catch (final IOException e) {
            TIS3D.getLog().warn("Invalid packet received.", e);
        }
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        final PacketBuffer buffer = new PacketBuffer(buf);
        try {
            buffer.writeInt(dimension);
            buffer.writeStringToBuffer(particleType);
            buffer.writeDouble(x);
            buffer.writeDouble(y);
            buffer.writeDouble(z);
        } catch (final IOException e) {
            TIS3D.getLog().warn("Failed sending packet.", e);
        }
    }
}
