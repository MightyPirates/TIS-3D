package li.cil.tis3d.common.network.message;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.ByteBuf;
import li.cil.tis3d.api.machine.Casing;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

public final class ClientCasingDataMessage extends AbstractCasingDataMessage {
    public ClientCasingDataMessage(final Casing casing, final ByteBuf data) {
        super(casing, data);
    }

    public ClientCasingDataMessage(final FriendlyByteBuf buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    public void handleMessage(final NetworkManager.PacketContext context) {
        final Level level = getServerLevel(context);
        if (level != null) {
            handleMessage(level);
        }
    }
}
