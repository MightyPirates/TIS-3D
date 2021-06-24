package li.cil.tis3d.common.network.message;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.common.tileentity.TileEntityCasing;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public final class CasingEnabledStateMessage extends AbstractMessageWithPosition {
    private boolean isEnabled;

    public CasingEnabledStateMessage(final Casing casing, final boolean isEnabled) {
        super(casing.getPosition());
        this.isEnabled = isEnabled;
    }

    public CasingEnabledStateMessage(final PacketBuffer buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    protected void handleMessage(final NetworkEvent.Context context) {
        final World world = getClientWorld();
        if (world != null) {
            withTileEntity(world, TileEntityCasing.class, casing ->
                casing.setEnabledClient(isEnabled));
        }
    }

    @Override
    public void fromBytes(final PacketBuffer buffer) {
        super.fromBytes(buffer);

        isEnabled = buffer.readBoolean();
    }

    @Override
    public void toBytes(final PacketBuffer buffer) {
        super.toBytes(buffer);

        buffer.writeBoolean(isEnabled);
    }
}
