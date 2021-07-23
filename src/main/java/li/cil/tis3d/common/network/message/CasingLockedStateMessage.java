package li.cil.tis3d.common.network.message;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.common.tileentity.CasingTileEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public final class CasingLockedStateMessage extends AbstractMessageWithPosition {
    private boolean isLocked;

    public CasingLockedStateMessage(final Casing casing, final boolean isLocked) {
        super(casing.getPosition());
        this.isLocked = isLocked;
    }

    public CasingLockedStateMessage(final FriendlyByteBuf buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    protected void handleMessage(final NetworkEvent.Context context) {
        final Level level = getClientLevel();
        if (level != null) {
            withTileEntity(level, CasingTileEntity.class, casing ->
                casing.setCasingLockedClient(isLocked));
        }
    }

    @Override
    public void fromBytes(final FriendlyByteBuf buffer) {
        super.fromBytes(buffer);

        isLocked = buffer.readBoolean();
    }

    @Override
    public void toBytes(final FriendlyByteBuf buffer) {
        super.toBytes(buffer);

        buffer.writeBoolean(isLocked);
    }
}
