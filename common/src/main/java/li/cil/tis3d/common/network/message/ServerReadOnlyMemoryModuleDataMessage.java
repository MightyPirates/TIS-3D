package li.cil.tis3d.common.network.message;

import dev.architectury.networking.NetworkManager;
import li.cil.tis3d.client.ClientHooks;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;

public final class ServerReadOnlyMemoryModuleDataMessage extends AbstractReadOnlyMemoryModuleDataMessage {
    public ServerReadOnlyMemoryModuleDataMessage(final InteractionHand hand, final byte[] data) {
        super(hand, data);
    }

    public ServerReadOnlyMemoryModuleDataMessage(final RegistryFriendlyByteBuf buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    public void handleMessage(final NetworkManager.PacketContext context) {
        ClientHooks.setReadOnlyMemoryModuleData(data);
    }
}
