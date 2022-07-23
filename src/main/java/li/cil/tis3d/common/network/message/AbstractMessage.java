package li.cil.tis3d.common.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
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

    public abstract void handleMessage(final NetworkEvent.Context context);

    public abstract void fromBytes(final FriendlyByteBuf buffer);

    public abstract void toBytes(final FriendlyByteBuf buffer);

    @Nullable
    protected Level getServerLevel(final NetworkEvent.Context context) {
        final ServerPlayer sender = context.getSender();
        return sender != null ? sender.getLevel() : null;
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    protected Level getClientLevel() {
        return Minecraft.getInstance().level;
    }
}
