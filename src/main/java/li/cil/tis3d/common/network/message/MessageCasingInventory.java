package li.cil.tis3d.common.network.message;

import io.netty.buffer.ByteBuf;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.common.TIS3D;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nullable;
import java.io.IOException;

public final class MessageCasingInventory extends AbstractMessageWithLocation {
    private int slot;
    private ItemStack stack;
    private NBTTagCompound moduleData;

    public MessageCasingInventory(final Casing casing, final int slot, @Nullable final ItemStack stack, @Nullable final NBTTagCompound moduleData) {
        super(casing.getCasingWorld(), casing.getPosition());
        this.slot = slot;
        this.stack = stack;
        this.moduleData = moduleData;
    }

    @SuppressWarnings("unused") // For deserialization.
    public MessageCasingInventory() {
    }

    // --------------------------------------------------------------------- //

    public int getSlot() {
        return slot;
    }

    public ItemStack getStack() {
        return stack;
    }

    public NBTTagCompound getModuleData() {
        return moduleData != null ? moduleData : new NBTTagCompound();
    }

    // --------------------------------------------------------------------- //

    @Override
    public void fromBytes(final ByteBuf buf) {
        super.fromBytes(buf);

        final PacketBuffer packet = new PacketBuffer(buf);

        slot = packet.readByte() & 0xFF;
        try {
            stack = packet.readItemStack();
        } catch (final IOException e) {
            TIS3D.getLog().warn("Failed parsing received ItemStack.", e);
            stack = null;
        }
        try {
            moduleData = packet.readCompoundTag();
        } catch (final IOException e) {
            TIS3D.getLog().warn("Failed parsing received NBTTagCompound.", e);
            moduleData = new NBTTagCompound();
        }
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        super.toBytes(buf);

        final PacketBuffer packet = new PacketBuffer(buf);

        packet.writeByte(slot);
        packet.writeItemStack(stack);
        packet.writeCompoundTag(moduleData);
    }
}
