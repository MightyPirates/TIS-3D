package li.cil.tis3d.common.network.message;

import dev.architectury.networking.NetworkManager;
import li.cil.tis3d.common.item.Items;
import li.cil.tis3d.common.item.ReadOnlyMemoryModuleItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public final class ClientReadOnlyMemoryModuleDataMessage extends AbstractReadOnlyMemoryModuleDataMessage {
    public ClientReadOnlyMemoryModuleDataMessage(final InteractionHand hand, final byte[] data) {
        super(hand, data);
    }

    public ClientReadOnlyMemoryModuleDataMessage(final FriendlyByteBuf buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    public void handleMessage(final NetworkManager.PacketContext context) {
        final var player = context.getPlayer();
        if (player != null) {
            final ItemStack stack = player.getItemInHand(hand);
            if (Items.is(stack, Items.READ_ONLY_MEMORY_MODULE)) {
                ReadOnlyMemoryModuleItem.saveToStack(stack, data);
            }
        }
    }
}
