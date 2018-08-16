package li.cil.tis3d.client.network.handler;

import li.cil.tis3d.client.gui.GuiHandlerClient;
import li.cil.tis3d.common.network.handler.AbstractMessageHandler;
import li.cil.tis3d.common.network.message.MessageOpenGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import pl.asie.protocharset.rift.network.NetworkContext;

public class MessageHandlerOpenGUI extends AbstractMessageHandler<MessageOpenGUI> {
	@Override
	protected void onMessageSynchronized(MessageOpenGUI message, NetworkContext context) {
		GuiScreen screen = GuiHandlerClient.getClientGuiElement(message.id, context.getPlayer().world, context.getPlayer());
		if (screen != null) {
			Minecraft.getMinecraft().displayGuiScreen(screen);
		}
	}
}
