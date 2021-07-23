package li.cil.tis3d.common.network.message;

import li.cil.tis3d.common.tileentity.ControllerTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public final class HaltAndCatchFireMessage extends AbstractMessageWithPosition {
    public HaltAndCatchFireMessage(final BlockPos position) {
        super(position);
    }

    public HaltAndCatchFireMessage(final FriendlyByteBuf buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    protected void handleMessage(final NetworkEvent.Context context) {
        final Level level = getClientLevel();
        if (level != null) {
            withTileEntity(level, ControllerTileEntity.class, ControllerTileEntity::haltAndCatchFire);
        }
    }
}
