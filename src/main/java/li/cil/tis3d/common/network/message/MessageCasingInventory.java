package li.cil.tis3d.common.network.message;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.charset.SendNetwork;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;

public final class MessageCasingInventory extends AbstractMessageWithLocation {
    @SendNetwork
    public int slot;
    @SendNetwork
    public ItemStack stack;
    @SendNetwork
    public CompoundTag moduleData;

    public MessageCasingInventory(final Casing casing, final int slot, final ItemStack stack, @Nullable final CompoundTag moduleData) {
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

    public CompoundTag getModuleData() {
        return moduleData != null ? moduleData : new CompoundTag();
    }
}
