package li.cil.tis3d.common.network.handler;

import li.cil.tis3d.common.init.Items;
import li.cil.tis3d.common.item.ReadOnlyMemoryModuleItem;
import li.cil.tis3d.common.network.message.MessageModuleReadOnlyMemoryData;
import net.fabricmc.fabric.networking.PacketContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public final class MessageHandlerModuleReadOnlyMemoryDataServer extends AbstractMessageHandler<MessageModuleReadOnlyMemoryData> {
    @Override
    protected void onMessageSynchronized(final MessageModuleReadOnlyMemoryData message, final PacketContext context) {
        final PlayerEntity player = context.getPlayer();
        if (player != null) {
            final ItemStack stack = player.getStackInHand(message.getHand());
            if (Items.isModuleReadOnlyMemory(stack)) {
                ReadOnlyMemoryModuleItem.saveToStack(stack, message.getData());
            }
        }
    }
}
