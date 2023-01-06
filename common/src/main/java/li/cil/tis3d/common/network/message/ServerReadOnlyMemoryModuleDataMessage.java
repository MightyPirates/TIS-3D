package li.cil.tis3d.common.network.message;

import dev.architectury.networking.NetworkManager;
import li.cil.tis3d.client.gui.ReadOnlyMemoryModuleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;

public final class ServerReadOnlyMemoryModuleDataMessage extends AbstractReadOnlyMemoryModuleDataMessage {
    public ServerReadOnlyMemoryModuleDataMessage(final InteractionHand hand, final byte[] data) {
        super(hand, data);
    }

    public ServerReadOnlyMemoryModuleDataMessage(final FriendlyByteBuf buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    public void handleMessage(final NetworkManager.PacketContext context) {
        final Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof final ReadOnlyMemoryModuleScreen moduleScreen) {
            moduleScreen.setData(data);
        }
    }
}
