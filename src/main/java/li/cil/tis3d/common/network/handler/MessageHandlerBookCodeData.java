package li.cil.tis3d.common.network.handler;

import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import li.cil.tis3d.common.init.Items;
import li.cil.tis3d.common.item.ItemBookCode;
import li.cil.tis3d.common.network.message.MessageBookCodeData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public final class MessageHandlerBookCodeData extends AbstractMessageHandler<MessageBookCodeData> {
    @Override
    protected void onMessageSynchronized(final MessageBookCodeData message, final MessageContext context) {
        final EntityPlayer player = context.getServerHandler().playerEntity;
        if (player != null) {
            final ItemStack stack = player.getHeldItem();
            if (Items.isBookCode(stack)) {
                final ItemBookCode.Data data = ItemBookCode.Data.loadFromNBT(message.getNbt());
                ItemBookCode.Data.saveToStack(stack, data);
            }
        }
    }
}
