package li.cil.tis3d.common.network;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import li.cil.tis3d.api.API;
import li.cil.tis3d.client.network.handler.MessageHandlerCasingState;
import li.cil.tis3d.client.network.handler.MessageHandlerHaltAndCatchFire;
import li.cil.tis3d.client.network.handler.MessageHandlerParticleEffects;
import li.cil.tis3d.common.network.handler.MessageHandlerBookCodeData;
import li.cil.tis3d.common.network.handler.MessageHandlerModuleData;
import li.cil.tis3d.common.network.message.MessageBookCodeData;
import li.cil.tis3d.common.network.message.MessageCasingState;
import li.cil.tis3d.common.network.message.MessageHaltAndCatchFire;
import li.cil.tis3d.common.network.message.MessageModuleData;
import li.cil.tis3d.common.network.message.MessageParticleEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public final class Network {
    public static final Network INSTANCE = new Network();

    public static final int RANGE_MEDIUM = 32;
    public static final int RANGE_LOW = 16;

    private static SimpleNetworkWrapper wrapper;

    // --------------------------------------------------------------------- //

    public void init() {
        wrapper = NetworkRegistry.INSTANCE.newSimpleChannel(API.MOD_ID);

        int discriminator = 1;

        wrapper.registerMessage(MessageHandlerModuleData.class, MessageModuleData.class, discriminator++, Side.CLIENT);
        wrapper.registerMessage(MessageHandlerModuleData.class, MessageModuleData.class, discriminator++, Side.SERVER);
        wrapper.registerMessage(MessageHandlerParticleEffects.class, MessageParticleEffect.class, discriminator++, Side.CLIENT);
        wrapper.registerMessage(MessageHandlerCasingState.class, MessageCasingState.class, discriminator++, Side.CLIENT);
        wrapper.registerMessage(MessageHandlerBookCodeData.class, MessageBookCodeData.class, discriminator++, Side.SERVER);
        wrapper.registerMessage(MessageHandlerHaltAndCatchFire.class, MessageHaltAndCatchFire.class, discriminator++, Side.CLIENT);
    }

    public SimpleNetworkWrapper getWrapper() {
        return wrapper;
    }

    // --------------------------------------------------------------------- //

    public static NetworkRegistry.TargetPoint getTargetPoint(final World world, final double x, final double y, final double z, final int range) {
        return new NetworkRegistry.TargetPoint(world.provider.dimensionId, x, y, z, range);
    }

    public static NetworkRegistry.TargetPoint getTargetPoint(final TileEntity tileEntity, final int range) {
        return getTargetPoint(tileEntity.getWorldObj(), tileEntity.xCoord + 0.5, tileEntity.yCoord + 0.5, tileEntity.zCoord + 0.5, range);
    }

    // --------------------------------------------------------------------- //

    private Network() {
    }
}
