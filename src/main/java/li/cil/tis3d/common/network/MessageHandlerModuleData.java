package li.cil.tis3d.common.network;

import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageHandlerModuleData implements IMessageHandler<MessageModuleData, IMessage> {
    @Override
    public IMessage onMessage(final MessageModuleData message, final MessageContext context) {
        final IThreadListener thread = FMLCommonHandler.instance().getWorldThread(context.netHandler);
        if (thread.isCallingFromMinecraftThread()) {
            process(message, context);
        } else {
            thread.addScheduledTask(() -> process(message, context));
        }
        return null;
    }

    private void process(final MessageModuleData message, final MessageContext context) {
        message.process(context);
    }
}
