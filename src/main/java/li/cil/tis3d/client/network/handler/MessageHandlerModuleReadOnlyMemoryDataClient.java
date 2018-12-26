package li.cil.tis3d.client.network.handler;

import li.cil.tis3d.client.gui.ReadOnlyMemoryModuleGui;
import li.cil.tis3d.common.network.handler.AbstractMessageHandler;
import li.cil.tis3d.common.network.message.MessageModuleReadOnlyMemoryData;
import net.fabricmc.fabric.networking.PacketContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Gui;

public final class MessageHandlerModuleReadOnlyMemoryDataClient extends AbstractMessageHandler<MessageModuleReadOnlyMemoryData> {
    @Override
    protected void onMessageSynchronized(final MessageModuleReadOnlyMemoryData message, final PacketContext context) {
        final Gui guiScreen = MinecraftClient.getInstance().currentGui;
        if (!(guiScreen instanceof ReadOnlyMemoryModuleGui)) {
            return;
        }

        final ReadOnlyMemoryModuleGui guiMemory = (ReadOnlyMemoryModuleGui) MinecraftClient.getInstance().currentGui;
        guiMemory.setData(message.getData());
    }
}
