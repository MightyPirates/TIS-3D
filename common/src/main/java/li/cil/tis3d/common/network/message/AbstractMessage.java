package li.cil.tis3d.common.network.message;

import dev.architectury.networking.NetworkManager;
import li.cil.tis3d.client.ClientHooks;
import li.cil.tis3d.common.network.Network;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

public abstract class AbstractMessage implements CustomPacketPayload {
    protected static final Logger LOGGER = LogManager.getLogger();

    protected AbstractMessage() {
    }

    protected AbstractMessage(final RegistryFriendlyByteBuf buffer) {
        fromBytes(buffer);
    }

    // --------------------------------------------------------------------- //
    // CustomPacketPayload

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return Network.getMessageType(getClass());
    }

    // --------------------------------------------------------------------- //

    public abstract void handleMessage(final NetworkManager.PacketContext context);

    public abstract void fromBytes(final RegistryFriendlyByteBuf buffer);

    public abstract void toBytes(final RegistryFriendlyByteBuf buffer);

    @Nullable
    protected Level getServerLevel(final NetworkManager.PacketContext context) {
        final var sender = context.getPlayer();
        return sender != null ? sender.level() : null;
    }

    @Nullable
    protected Level getClientLevel() {
        return ClientHooks.getLevel();
    }
}
