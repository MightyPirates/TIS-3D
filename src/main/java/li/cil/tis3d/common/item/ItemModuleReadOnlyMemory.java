package li.cil.tis3d.common.item;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.common.TIS3D;
import li.cil.tis3d.common.gui.GuiHandlerCommon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public final class ItemModuleReadOnlyMemory extends ItemModule {
    private static final String TAG_DATA = "data";
    private static final byte[] EMPTY_DATA = new byte[0];

    public ItemModuleReadOnlyMemory() {
        setMaxStackSize(1);
    }

    // --------------------------------------------------------------------- //
    // Item

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        player.openGui(TIS3D.instance, GuiHandlerCommon.GuiId.MODULE_MEMORY.ordinal(), world, 0, 0, 0);
        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public boolean doesSneakBypassUse(final ItemStack stack, final IBlockAccess world, final BlockPos pos, final EntityPlayer player) {
        return world.getTileEntity(pos) instanceof Casing;
    }

    // --------------------------------------------------------------------- //

    /**
     * Load ROM data from the specified NBT tag.
     *
     * @param nbt the tag to load the data from.
     * @return the data loaded from the tag.
     */
    public static byte[] loadFromNBT(@Nullable final NBTTagCompound nbt) {
        if (nbt != null) {
            return nbt.getByteArray(TAG_DATA);
        }
        return EMPTY_DATA;
    }

    /**
     * Load ROM data from the specified item stack.
     *
     * @param stack the item stack to load the data from.
     * @return the data loaded from the stack.
     */
    public static byte[] loadFromStack(final ItemStack stack) {
        return loadFromNBT(stack.getTagCompound());
    }

    /**
     * Save the specified ROM data to the specified item stack.
     *
     * @param stack the item stack to save the data to.
     * @param data  the data to save to the item stack.
     */
    public static void saveToStack(final ItemStack stack, final byte[] data) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            stack.setTagCompound(nbt = new NBTTagCompound());
        }
        nbt.setByteArray(TAG_DATA, data);
    }
}
