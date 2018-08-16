package li.cil.tis3d.common.network.handler;

import li.cil.tis3d.common.init.Items;
import li.cil.tis3d.common.item.ItemBookCode;
import li.cil.tis3d.common.network.message.MessageBookCodeData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import pl.asie.protocharset.rift.network.NetworkContext;

public final class MessageHandlerBookCodeData extends AbstractMessageHandler<MessageBookCodeData> {
    @Override
    protected void onMessageSynchronized(final MessageBookCodeData message, final NetworkContext context) {
        final EntityPlayer player = context.getPlayer();
        if (player != null) {
            final ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
            if (Items.isBookCode(stack)) {
                final ItemBookCode.Data data = ItemBookCode.Data.loadFromNBT(message.getNbt());
                ItemBookCode.Data.saveToStack(stack, data);
            }
        }
    }
}
