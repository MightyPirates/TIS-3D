package li.cil.tis3d.common;

import li.cil.tis3d.Constants;
import li.cil.tis3d.client.network.MessageHandlerModuleData;
import li.cil.tis3d.common.network.MessageModuleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class Network {
    public static final Network INSTANCE = new Network();

    private static SimpleNetworkWrapper wrapper;

    public void init() {
        wrapper = NetworkRegistry.INSTANCE.newSimpleChannel(Constants.MOD_ID);
        wrapper.registerMessage(MessageHandlerModuleData.class, MessageModuleData.class, 1, Side.CLIENT);
    }

    public static NetworkRegistry.TargetPoint getTargetPoint(final TileEntity tileEntity, final double range) {
        return new NetworkRegistry.TargetPoint(
                tileEntity.getWorld().provider.getDimensionId(),
                tileEntity.getPos().getX() + 0.5,
                tileEntity.getPos().getY() + 0.5,
                tileEntity.getPos().getZ() + 0.5,
                range);
    }

    public SimpleNetworkWrapper getWrapper() {
        return wrapper;
    }

    // --------------------------------------------------------------------- //

    private Network() {
    }
}
