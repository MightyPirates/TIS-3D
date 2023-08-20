package li.cil.tis3d.common.network.message;

import dev.architectury.networking.NetworkManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

public abstract class AbstractMessage {
    protected static final Logger LOGGER = LogManager.getLogger();

    protected AbstractMessage() {
    }

    protected AbstractMessage(final FriendlyByteBuf buffer) {
        fromBytes(buffer);
    }

    // --------------------------------------------------------------------- //

    public abstract void handleMessage(final NetworkManager.PacketContext context);

    public abstract void fromBytes(final FriendlyByteBuf buffer);

    public abstract void toBytes(final FriendlyByteBuf buffer);

    @Nullable
    protected Level getServerLevel(final NetworkManager.PacketContext context) {
        final var sender = context.getPlayer();
        return sender != null ? sender.level() : null;
    }

    @Environment(EnvType.CLIENT)
    @Nullable
    protected Level getClientLevel() {
        return Minecraft.getInstance().level;
    }
}
