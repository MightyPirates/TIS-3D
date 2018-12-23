package li.cil.tis3d.common.network.handler;

import li.cil.tis3d.common.init.Items;
import li.cil.tis3d.common.item.ItemModuleReadOnlyMemory;
import li.cil.tis3d.common.network.message.MessageModuleReadOnlyMemoryData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import li.cil.tis3d.charset.NetworkContext;

public final class MessageHandlerModuleReadOnlyMemoryDataServer extends AbstractMessageHandler<MessageModuleReadOnlyMemoryData> {
    @Override
    protected void onMessageSynchronized(final MessageModuleReadOnlyMemoryData message, final NetworkContext context) {
        final PlayerEntity player = context.getPlayer();
        if (player != null) {
            final ItemStack stack = player.getStackInHand(Hand.MAIN);
            if (Items.isModuleReadOnlyMemory(stack)) {
                ItemModuleReadOnlyMemory.saveToStack(stack, message.getData());
            }
        }
    }
}
