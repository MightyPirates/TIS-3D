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
    private CompoundNBT nbt;

    public CodeBookDataMessage(final Hand hand, final CompoundNBT nbt) {
        this.hand = hand;
        this.nbt = nbt;
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

        final ItemStack stack = player.getHeldItem(hand);
        if (Items.is(stack, Items.BOOK_CODE)) {
            final ItemBookCode.Data data = ItemBookCode.Data.loadFromNBT(nbt);
            ItemBookCode.Data.saveToStack(stack, data);
        }
    }

    @Override
    public void fromBytes(final PacketBuffer buffer) {
        hand = buffer.readEnumValue(Hand.class);
        nbt = buffer.readCompoundTag();
    }

    @Override
    public void toBytes(final PacketBuffer buffer) {
        buffer.writeEnumValue(hand);
        buffer.writeCompoundTag(nbt);
    }
}
