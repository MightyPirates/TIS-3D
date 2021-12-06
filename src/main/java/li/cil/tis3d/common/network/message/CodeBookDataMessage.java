package li.cil.tis3d.common.network.message;

import li.cil.tis3d.common.item.CodeBookItem;
import li.cil.tis3d.common.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public final class CodeBookDataMessage extends AbstractMessage {
    private InteractionHand hand;
    private CompoundTag tag;

    public CodeBookDataMessage(final InteractionHand hand, final CompoundTag tag) {
        this.hand = hand;
        this.tag = tag;
    }

    public CodeBookDataMessage(final FriendlyByteBuf buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    protected void handleMessage(final NetworkEvent.Context context) {
        final ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }

        final ItemStack stack = player.getItemInHand(hand);
        if (Items.is(stack, Items.BOOK_CODE)) {
            final CodeBookItem.Data data = CodeBookItem.Data.loadFromTag(tag);
            CodeBookItem.Data.saveToStack(stack, data);
        }
    }

    @Override
    public void fromBytes(final FriendlyByteBuf buffer) {
        hand = buffer.readEnum(InteractionHand.class);
        tag = buffer.readNbt();
    }

    @Override
    public void toBytes(final FriendlyByteBuf buffer) {
        buffer.writeEnum(hand);
        buffer.writeNbt(tag);
    }
}
