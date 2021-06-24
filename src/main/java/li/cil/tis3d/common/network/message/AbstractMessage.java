package li.cil.tis3d.common.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public abstract class AbstractMessage {
    protected static final Logger LOGGER = LogManager.getLogger();

    protected AbstractMessage() {
    }

    protected AbstractMessage(final PacketBuffer buffer) {
        fromBytes(buffer);
    }

    // --------------------------------------------------------------------- //

    public static boolean handleMessage(final AbstractMessage message, final Supplier<NetworkEvent.Context> contextSupplied) {
        final NetworkEvent.Context context = contextSupplied.get();
        context.enqueueWork(() -> message.handleMessage(context));
        return true;
    }

    protected abstract void handleMessage(final NetworkEvent.Context context);

    public abstract void fromBytes(final PacketBuffer buffer);

    public abstract void toBytes(final PacketBuffer buffer);

    @Nullable
    protected World getServerWorld(final NetworkEvent.Context context) {
        final ServerPlayerEntity sender = context.getSender();
        return sender != null ? sender.getLevel() : null;
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    protected World getClientWorld() {
        return Minecraft.getInstance().level;
    }
}
