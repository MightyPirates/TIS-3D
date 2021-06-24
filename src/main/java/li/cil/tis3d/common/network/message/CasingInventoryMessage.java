package li.cil.tis3d.common.network.message;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.common.tileentity.TileEntityCasing;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;

public final class CasingInventoryMessage extends AbstractMessageWithPosition {
    private int slot;
    private ItemStack stack;
    private CompoundNBT moduleData;

    public CasingInventoryMessage(final Casing casing, final int slot, final ItemStack stack, @Nullable final CompoundNBT moduleData) {
        super(casing.getPosition());
        this.slot = slot;
        this.stack = stack;
        this.moduleData = moduleData;
    }

    public CasingInventoryMessage(final PacketBuffer buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    protected void handleMessage(final NetworkEvent.Context context) {
        final World world = getClientWorld();
        if (world != null) {
            withTileEntity(world, TileEntityCasing.class, casing ->
                casing.setStackAndModuleClient(slot, stack, moduleData));
        }
    }

    @Override
    public void fromBytes(final PacketBuffer buffer) {
        super.fromBytes(buffer);

        slot = buffer.readUnsignedByte();
        stack = buffer.readItemStack();
        moduleData = buffer.readCompoundTag();
    }

    @Override
    public void toBytes(final PacketBuffer buffer) {
        super.toBytes(buffer);

        buffer.writeByte(slot);
        buffer.writeItemStack(stack);
        buffer.writeCompoundTag(moduleData);
    }
}
