package li.cil.tis3d.common.network.message;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.common.tileentity.CasingTileEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public final class CasingLockedStateMessage extends AbstractMessageWithPosition {
    private boolean isLocked;

    public CasingLockedStateMessage(final Casing casing, final boolean isLocked) {
        super(casing.getPosition());
        this.isLocked = isLocked;
    }

    public CasingLockedStateMessage(final PacketBuffer buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    protected void handleMessage(final NetworkEvent.Context context) {
        final World world = getClientWorld();
        if (world != null) {
            withTileEntity(world, CasingTileEntity.class, casing ->
                casing.setCasingLockedClient(isLocked));
        }
    }

    @Override
    public void fromBytes(final PacketBuffer buffer) {
        super.fromBytes(buffer);

        isLocked = buffer.readBoolean();
    }

    @Override
    public void toBytes(final PacketBuffer buffer) {
        super.toBytes(buffer);

        buffer.writeBoolean(isLocked);
    }
}
