/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.common.network.message;

import dev.architectury.networking.NetworkManager;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

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

    public CasingInventoryMessage(final RegistryFriendlyByteBuf buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    public void handleMessage(final NetworkManager.PacketContext context) {
        final Level level = getClientLevel();
        if (level != null) {
            withBlockEntity(level, CasingBlockEntity.class, casing ->
                casing.setStackAndModuleClient(slot, stack, moduleData));
        }
    }

    @Override
    public void fromBytes(final RegistryFriendlyByteBuf buffer) {
        super.fromBytes(buffer);

        slot = buffer.readUnsignedByte();
        stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
        moduleData = buffer.readNbt();
    }

    @Override
    public void toBytes(final RegistryFriendlyByteBuf buffer) {
        super.toBytes(buffer);

        buffer.writeByte(slot);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, stack);
        buffer.writeNbt(moduleData);
    }
}
