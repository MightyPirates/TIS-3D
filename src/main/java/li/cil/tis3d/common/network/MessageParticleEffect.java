package li.cil.tis3d.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class MessageParticleEffect extends AbstractMessage {
    private int dimension;
    private EnumParticleTypes particleType;
    private double x;
    private double y;
    private double z;

    public MessageParticleEffect(final World world, final EnumParticleTypes particleType, final double x, final double y, final double z) {
        this.dimension = world.provider.getDimensionId();
        this.particleType = particleType;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public MessageParticleEffect() {
    }

    // --------------------------------------------------------------------- //

    public void process(final MessageContext context) {
        final World world = getWorld(context, dimension);
        if (world != null) {
            world.spawnParticle(particleType, x, y, z, 0, 0, 0);
        }
    }

    // --------------------------------------------------------------------- //

    @Override
    public void fromBytes(final ByteBuf buf) {
        final PacketBuffer buffer = new PacketBuffer(buf);
        dimension = buffer.readInt();
        particleType = buffer.readEnumValue(EnumParticleTypes.class);
        x = buffer.readDouble();
        y = buffer.readDouble();
        z = buffer.readDouble();
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        final PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeInt(dimension);
        buffer.writeEnumValue(particleType);
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
    }
}
