package li.cil.tis3d.common;

import li.cil.tis3d.Constants;
import li.cil.tis3d.api.Casing;
import li.cil.tis3d.client.network.MessageHandlerParticleEffects;
import li.cil.tis3d.common.network.MessageHandlerModuleData;
import li.cil.tis3d.common.network.MessageModuleData;
import li.cil.tis3d.common.network.MessageParticleEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class Network {
    public static final Network INSTANCE = new Network();

    private static SimpleNetworkWrapper wrapper;

    public void init() {
        wrapper = NetworkRegistry.INSTANCE.newSimpleChannel(Constants.MOD_ID);
        wrapper.registerMessage(MessageHandlerModuleData.class, MessageModuleData.class, 1, Side.CLIENT);
        wrapper.registerMessage(MessageHandlerModuleData.class, MessageModuleData.class, 2, Side.SERVER);
        wrapper.registerMessage(MessageHandlerParticleEffects.class, MessageParticleEffect.class, 3, Side.CLIENT);
    }

    private static NetworkRegistry.TargetPoint getTargetPoint(final World world, final double x, final double y, final double z, final int range) {
        return new NetworkRegistry.TargetPoint(world.provider.getDimensionId(), x, y, z, range);
    }

    private static NetworkRegistry.TargetPoint getTargetPoint(final World world, final BlockPos position, final int range) {
        return getTargetPoint(world, position.getX() + 0.5, position.getY() + 0.5, position.getZ() + 0.5, range);
    }

    public SimpleNetworkWrapper getWrapper() {
        return wrapper;
    }

    public void sendModuleData(final Casing casing, final MessageModuleData message) {
        final NetworkRegistry.TargetPoint point = Network.getTargetPoint(casing.getWorld(), casing.getPosition(), 32);
        Network.INSTANCE.getWrapper().sendToAllAround(message, point);
    }

    public void spawnParticles(final World world, final EnumParticleTypes particleType, final double x, final double y, final double z) {
        getWrapper().sendToAllAround(new MessageParticleEffect(world, particleType, x, y, z), getTargetPoint(world, x, y, z, 16));
    }

    // --------------------------------------------------------------------- //

    private Network() {
    }
}
