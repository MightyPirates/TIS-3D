package li.cil.tis3d.client.network.handler;

import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import li.cil.tis3d.client.gui.GuiModuleMemory;
import li.cil.tis3d.common.network.handler.AbstractMessageHandler;
import li.cil.tis3d.common.network.message.MessageModuleReadOnlyMemoryData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public final class MessageHandlerModuleReadOnlyMemoryDataClient extends AbstractMessageHandler<MessageModuleReadOnlyMemoryData> {
    @Override
    protected void onMessageSynchronized(final MessageModuleReadOnlyMemoryData message, final MessageContext context) {
        final GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;
        if (!(guiScreen instanceof GuiModuleMemory)) {
            return;
        }

        final GuiModuleMemory guiMemory = (GuiModuleMemory) Minecraft.getMinecraft().currentScreen;
        guiMemory.setData(message.getData());
    }
}
