package li.cil.tis3d.common.network.message;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

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
    public void handleMessage(final NetworkEvent.Context context) {
        final Level level = getClientLevel();
        if (level != null) {
            withBlockEntity(level, CasingBlockEntity.class, casing ->
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
