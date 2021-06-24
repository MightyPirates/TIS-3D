package li.cil.tis3d.common.network.message;

import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public final class RedstoneParticleEffectMessage extends AbstractMessage {
    private double x;
    private double y;
    private double z;

    public RedstoneParticleEffectMessage(final double x, final double y, final double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public RedstoneParticleEffectMessage(final PacketBuffer buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    protected void handleMessage(final NetworkEvent.Context context) {
        final World world = getClientWorld();
        if (world != null) {
            world.addParticle(RedstoneParticleData.REDSTONE_DUST, x, y, z, 0, 0, 0);
        }
    }

    @Override
    public void fromBytes(final PacketBuffer buffer) {
        x = buffer.readDouble();
        y = buffer.readDouble();
        z = buffer.readDouble();
    }

    @Override
    public void toBytes(final PacketBuffer buffer) {
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
    }
}
