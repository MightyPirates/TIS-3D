package li.cil.tis3d.common.network.message;

import li.cil.tis3d.common.item.ItemModuleReadOnlyMemory;
import li.cil.tis3d.common.item.Items;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;

public final class ClientReadOnlyMemoryModuleDataMessage extends AbstractReadOnlyMemoryModuleDataMessage {
    public ClientReadOnlyMemoryModuleDataMessage(final Hand hand, final byte[] data) {
        super(hand, data);
    }

    public ClientReadOnlyMemoryModuleDataMessage(final PacketBuffer buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    protected void handleMessage(final NetworkEvent.Context context) {
        final ServerPlayerEntity player = context.getSender();
        if (player != null) {
            final ItemStack stack = player.getHeldItem(hand);
            if (Items.is(stack, Items.READ_ONLY_MEMORY_MODULE)) {
                ItemModuleReadOnlyMemory.saveToStack(stack, data);
            }
        }
    }
}
