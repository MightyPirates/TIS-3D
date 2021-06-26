package li.cil.tis3d.common.network.message;

import li.cil.tis3d.common.tileentity.ControllerTileEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public final class HaltAndCatchFireMessage extends AbstractMessageWithPosition {
    public HaltAndCatchFireMessage(final BlockPos position) {
        super(position);
    }

    public HaltAndCatchFireMessage(final PacketBuffer buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    protected void handleMessage(final NetworkEvent.Context context) {
        final World world = getClientWorld();
        if (world != null) {
            withTileEntity(world, ControllerTileEntity.class, ControllerTileEntity::haltAndCatchFire);
        }
    }
}
