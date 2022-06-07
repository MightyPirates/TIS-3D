package li.cil.tis3d.common.network.message;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;

public final class CasingInventoryMessage extends AbstractMessageWithPosition {
    private int slot;
    private ItemStack stack;
    private CompoundTag moduleData;

    public CasingInventoryMessage(final Casing casing, final int slot, final ItemStack stack, @Nullable final CompoundTag moduleData) {
        super(casing.getPosition());
        this.slot = slot;
        this.stack = stack;
        this.moduleData = moduleData;
    }

    public CasingInventoryMessage(final FriendlyByteBuf buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    protected void handleMessage(final NetworkEvent.Context context) {
        final Level level = getClientLevel();
        if (level != null) {
            withBlockEntity(level, CasingBlockEntity.class, casing ->
                casing.setStackAndModuleClient(slot, stack, moduleData));
        }
    }

    @Override
    public void fromBytes(final FriendlyByteBuf buffer) {
        super.fromBytes(buffer);

        slot = buffer.readUnsignedByte();
        stack = buffer.readItem();
        moduleData = buffer.readNbt();
    }

    @Override
    public void toBytes(final FriendlyByteBuf buffer) {
        super.toBytes(buffer);

        buffer.writeByte(slot);
        buffer.writeItemStack(stack, false);
        buffer.writeNbt(moduleData);
    }
}
