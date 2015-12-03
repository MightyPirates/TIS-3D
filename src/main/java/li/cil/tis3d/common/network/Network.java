package li.cil.tis3d.common.network;

import li.cil.tis3d.Constants;
import li.cil.tis3d.client.network.handler.MessageHandlerCasingState;
import li.cil.tis3d.client.network.handler.MessageHandlerParticleEffects;
import li.cil.tis3d.common.network.handler.MessageHandlerModuleData;
import li.cil.tis3d.common.network.message.MessageCasingState;
import li.cil.tis3d.common.network.message.MessageModuleData;
import li.cil.tis3d.common.network.message.MessageParticleEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class Network {
    public static final Network INSTANCE = new Network();

    public static final int RANGE_MEDIUM = 32;
    public static final int RANGE_LOW = 16;

    private static SimpleNetworkWrapper wrapper;

    // --------------------------------------------------------------------- //

    public void init() {
        wrapper = NetworkRegistry.INSTANCE.newSimpleChannel(Constants.MOD_ID);
        wrapper.registerMessage(MessageHandlerModuleData.class, MessageModuleData.class, 1, Side.CLIENT);
        wrapper.registerMessage(MessageHandlerModuleData.class, MessageModuleData.class, 2, Side.SERVER);
        wrapper.registerMessage(MessageHandlerParticleEffects.class, MessageParticleEffect.class, 3, Side.CLIENT);
        wrapper.registerMessage(MessageHandlerCasingState.class, MessageCasingState.class, 4, Side.CLIENT);
    }

    public SimpleNetworkWrapper getWrapper() {
        return wrapper;
    }

    public static void spawnParticles(final World world, final EnumParticleTypes particleType, final double x, final double y, final double z) {
        Network.INSTANCE.getWrapper().sendToAllAround(new MessageParticleEffect(world, particleType, x, y, z), getTargetPoint(world, x, y, z, RANGE_LOW));
    }

    // --------------------------------------------------------------------- //

    public static NetworkRegistry.TargetPoint getTargetPoint(final World world, final double x, final double y, final double z, final int range) {
        return new NetworkRegistry.TargetPoint(world.provider.getDimensionId(), x, y, z, range);
    }

    public static NetworkRegistry.TargetPoint getTargetPoint(final World world, final BlockPos position, final int range) {
        return getTargetPoint(world, position.getX() + 0.5, position.getY() + 0.5, position.getZ() + 0.5, range);
    }

    public static NetworkRegistry.TargetPoint getTargetPoint(final TileEntity tileEntity, final int range) {
        return getTargetPoint(tileEntity.getWorld(), tileEntity.getPos(), range);
    }

    // --------------------------------------------------------------------- //

    private Network() {
    }
}
