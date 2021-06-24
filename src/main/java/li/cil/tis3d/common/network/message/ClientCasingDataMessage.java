package li.cil.tis3d.common.network.message;

import io.netty.buffer.ByteBuf;
import li.cil.tis3d.api.machine.Casing;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public final class ClientCasingDataMessage extends AbstractCasingDataMessage {
    public ClientCasingDataMessage(final Casing casing, final ByteBuf data) {
        super(casing, data);
    }

    public ClientCasingDataMessage(final PacketBuffer buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    protected void handleMessage(final NetworkEvent.Context context) {
        final World world = getServerWorld(context);
        if (world != null) {
            handleMessage(world);
        }
    }
}
