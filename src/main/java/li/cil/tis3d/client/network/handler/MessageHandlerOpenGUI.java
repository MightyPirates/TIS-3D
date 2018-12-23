package li.cil.tis3d.client.network.handler;

import li.cil.tis3d.client.gui.GuiHandlerClient;
import li.cil.tis3d.common.network.handler.AbstractMessageHandler;
import li.cil.tis3d.common.network.message.MessageOpenGUI;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Gui;
import li.cil.tis3d.charset.NetworkContext;

public class MessageHandlerOpenGUI extends AbstractMessageHandler<MessageOpenGUI> {
	@Override
	protected void onMessageSynchronized(MessageOpenGUI message, NetworkContext context) {
		Gui screen = GuiHandlerClient.getClientGuiElement(message.id, context.getPlayer().world, context.getPlayer());
		if (screen != null) {
			MinecraftClient.getInstance().openGui(screen);
		}
	}
}
