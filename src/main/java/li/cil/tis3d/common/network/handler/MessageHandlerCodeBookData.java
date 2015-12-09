package li.cil.tis3d.common.network.handler;

import li.cil.tis3d.common.item.ItemCodeBook;
import li.cil.tis3d.common.network.message.MessageCodeBookData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class MessageHandlerCodeBookData extends AbstractMessageHandler<MessageCodeBookData> {
    @Override
    protected void process(final MessageCodeBookData message, final MessageContext context) {
        final EntityPlayer player = context.getServerHandler().playerEntity;
        if (player != null) {
            final ItemStack stack = player.getHeldItem();
            if (ItemCodeBook.isCodeBook(stack)) {
                final ItemCodeBook.Data data = ItemCodeBook.Data.loadFromNBT(message.getNbt());
                ItemCodeBook.Data.saveToStack(stack, data);
            }
        }
    }
}
