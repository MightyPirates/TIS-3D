package li.cil.tis3d.common.network.message;

import li.cil.tis3d.common.item.ItemBookCode;
import li.cil.tis3d.common.item.Items;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;

public final class CodeBookDataMessage extends AbstractMessage {
    private Hand hand;
    private CompoundNBT tag;

    public CodeBookDataMessage(final Hand hand, final CompoundNBT tag) {
        this.hand = hand;
        this.tag = tag;
    }

    public CodeBookDataMessage(final PacketBuffer buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    protected void handleMessage(final NetworkEvent.Context context) {
        final ServerPlayerEntity player = context.getSender();
        if (player == null) {
            return;
        }

        final ItemStack stack = player.getItemInHand(hand);
        if (Items.is(stack, Items.BOOK_CODE)) {
            final ItemBookCode.Data data = ItemBookCode.Data.loadFromNBT(tag);
            ItemBookCode.Data.saveToStack(stack, data);
        }
    }

    @Override
    public void fromBytes(final PacketBuffer buffer) {
        hand = buffer.readEnum(Hand.class);
        tag = buffer.readNbt();
    }

    @Override
    public void toBytes(final PacketBuffer buffer) {
        buffer.writeEnum(hand);
        buffer.writeNbt(tag);
    }
}
