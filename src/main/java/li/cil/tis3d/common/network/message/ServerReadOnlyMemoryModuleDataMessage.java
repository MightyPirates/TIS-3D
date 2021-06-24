package li.cil.tis3d.common.network.message;

import li.cil.tis3d.client.gui.ReadOnlyMemoryModuleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;

public final class ServerReadOnlyMemoryModuleDataMessage extends AbstractReadOnlyMemoryModuleDataMessage {
    public ServerReadOnlyMemoryModuleDataMessage(final Hand hand, final byte[] data) {
        super(hand, data);
    }

    public ServerReadOnlyMemoryModuleDataMessage(final PacketBuffer buffer) {
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
