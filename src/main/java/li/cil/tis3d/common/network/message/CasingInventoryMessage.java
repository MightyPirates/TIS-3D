package li.cil.tis3d.common.network.message;

import io.netty.buffer.ByteBuf;
import li.cil.tis3d.api.machine.Casing;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import javax.annotation.Nullable;

public final class CasingInventoryMessage extends AbstractMessageWithLocation {
    private int slot;
    private ItemStack stack;
    private CompoundTag moduleData;

    public CasingInventoryMessage(final Casing casing, final int slot, final ItemStack stack, @Nullable final CompoundTag moduleData) {
        super(casing.getCasingWorld(), casing.getPosition());
        this.slot = slot;
        this.stack = stack;
        this.moduleData = moduleData;
    }

    @SuppressWarnings("unused") // For deserialization.
    public CasingInventoryMessage() {
    }

    // --------------------------------------------------------------------- //

    public int getSlot() {
        return slot;
    }

    public ItemStack getStack() {
        return stack;
    }

    public CompoundTag getModuleData() {
        return moduleData != null ? moduleData : new CompoundTag();
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    public void fromBytes(final ByteBuf buf) {
        super.fromBytes(buf);

        final PacketByteBuf packet = new PacketByteBuf(buf);

        slot = packet.readByte() & 0xFF;
        stack = packet.readItemStack();
        moduleData = packet.readCompoundTag();
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        super.toBytes(buf);

        final PacketByteBuf packet = new PacketByteBuf(buf);

        packet.writeByte(slot);
        packet.writeItemStack(stack);
        packet.writeCompoundTag(moduleData);
    }
}
