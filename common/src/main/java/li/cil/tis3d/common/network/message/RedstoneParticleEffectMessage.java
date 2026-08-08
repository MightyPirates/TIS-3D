package li.cil.tis3d.common.network.message;

import dev.architectury.networking.NetworkManager;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.Level;

public final class RedstoneParticleEffectMessage extends AbstractMessage {
    private double x;
    private double y;
    private double z;

    public RedstoneParticleEffectMessage(final double x, final double y, final double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public RedstoneParticleEffectMessage(final RegistryFriendlyByteBuf buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    public void handleMessage(final NetworkManager.PacketContext context) {
        final Level level = getClientLevel();
        if (level != null) {
            level.addParticle(DustParticleOptions.REDSTONE, x, y, z, 0, 0, 0);
        }
    }

    @Override
    public void fromBytes(final RegistryFriendlyByteBuf buffer) {
        x = buffer.readDouble();
        y = buffer.readDouble();
        z = buffer.readDouble();
    }

    @Override
    public void toBytes(final RegistryFriendlyByteBuf buffer) {
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
    }
}
