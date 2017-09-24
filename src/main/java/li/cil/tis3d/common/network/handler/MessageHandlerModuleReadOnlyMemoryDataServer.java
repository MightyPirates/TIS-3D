package li.cil.tis3d.common.network.handler;

import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import li.cil.tis3d.common.init.Items;
import li.cil.tis3d.common.item.ItemModuleReadOnlyMemory;
import li.cil.tis3d.common.network.message.MessageModuleReadOnlyMemoryData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public final class MessageHandlerModuleReadOnlyMemoryDataServer extends AbstractMessageHandler<MessageModuleReadOnlyMemoryData> {
    @Override
    protected void onMessageSynchronized(final MessageModuleReadOnlyMemoryData message, final MessageContext context) {
        final EntityPlayer player = context.getServerHandler().playerEntity;
        if (player != null) {
            final ItemStack stack = player.getHeldItem();
            if (Items.isModuleReadOnlyMemory(stack)) {
                ItemModuleReadOnlyMemory.saveToStack(stack, message.getData());
            }
        }
    }
}
