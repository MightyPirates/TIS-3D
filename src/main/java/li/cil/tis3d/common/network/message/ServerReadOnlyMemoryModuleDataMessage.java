package li.cil.tis3d.common.network.message;

import li.cil.tis3d.client.gui.ReadOnlyMemoryModuleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

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
    protected void handleMessage(final NetworkEvent.Context context) {
        final Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof ReadOnlyMemoryModuleScreen) {
            ((ReadOnlyMemoryModuleScreen) screen).setData(data);
        }
    }
}
