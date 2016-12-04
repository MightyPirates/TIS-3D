package li.cil.tis3d.common.item;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.client.gui.GuiHandlerClient;
import li.cil.tis3d.common.TIS3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The code book, utility book for coding ASM programs for execution modules.
 */
public final class ItemBookCode extends ItemBook {

    public ItemBookCode() {
        setMaxStackSize(1);
    }

    // --------------------------------------------------------------------- //
    // Item

    @SideOnly(Side.CLIENT)
    @Override
    public FontRenderer getFontRenderer(final ItemStack stack) {
        return Minecraft.getMinecraft().fontRendererObj;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(final ItemStack stack, final EntityPlayer playerIn, final List<String> tooltip, final boolean advanced) {
        super.addInformation(stack, playerIn, tooltip, advanced);
        final String info = I18n.translateToLocal(li.cil.tis3d.common.Constants.TOOLTIP_BOOK_CODE);
        tooltip.addAll(getFontRenderer(stack).listFormattedStringToWidth(info, li.cil.tis3d.common.Constants.MAX_TOOLTIP_WIDTH));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        if (world.isRemote) {
            player.openGui(TIS3D.instance, GuiHandlerClient.ID_GUI_BOOK_CODE, world, 0, 0, 0);
        }
        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public boolean doesSneakBypassUse(final ItemStack stack, final IBlockAccess world, final BlockPos pos, final EntityPlayer player) {
        return world.getTileEntity(pos) instanceof Casing;
    }

    // --------------------------------------------------------------------- //
    // ItemBook

    @Override
    public boolean isEnchantable(final ItemStack stack) {
        return false;
    }

    @Override
    public int getItemEnchantability() {
        return 0;
    }

    // --------------------------------------------------------------------- //

    /**
     * Wrapper for list of programs stored in the code book.
     */
    public static class Data {
        public static final Pattern PATTERN_LINES = Pattern.compile("\r?\n");
        private static final String TAG_PAGES = "pages";
        private static final String TAG_SELECTED = "selected";

        private final List<List<String>> programs = new ArrayList<>();
        private int selectedProgram = 0;

        // --------------------------------------------------------------------- //

        /**
         * Get the program currently selected in the book.
         *
         * @return the index of the selected program.
         */
        public int getSelectedProgram() {
            return selectedProgram;
        }

        /**
         * Set which program is currently selected.
         *
         * @param index the new selected index.
         */
        public void setSelectedProgram(final int index) {
            this.selectedProgram = index;
            validateSelectedPage();
        }

        /**
         * Get the number of programs stored in the book.
         *
         * @return the number of programs stored in the book.
         */
        public int getProgramCount() {
            return programs.size();
        }

        /**
         * Get the code of the program with the specified index.
         *
         * @param index the index of the program to get.
         * @return the code of the program.
         */
        public List<String> getProgram(final int index) {
            return programs.get(index);
        }

        /**
         * Add a new program to the book.
         *
         * @param code the code of the program to add.
         */
        public void addProgram(final List<String> code) {
            programs.add(code);
        }

        /**
         * Overwrite a program at the specified index.
         *
         * @param page the index of the program to overwrite.
         * @param code the code of the program.
         */
        public void setProgram(final int page, final List<String> code) {
            programs.set(page, code);
        }

        /**
         * Remove a program from the book.
         *
         * @param index the index of the program to remove.
         */
        public void removeProgram(final int index) {
            programs.remove(index);
            validateSelectedPage();
        }

        /**
         * Load data from the specified NBT tag.
         *
         * @param nbt the tag to load the data from.
         */
        public void readFromNBT(final NBTTagCompound nbt) {
            programs.clear();

            final NBTTagList pagesNbt = nbt.getTagList(TAG_PAGES, Constants.NBT.TAG_STRING);
            for (int index = 0; index < pagesNbt.tagCount(); index++) {
                programs.add(Arrays.asList(PATTERN_LINES.split(pagesNbt.getStringTagAt(index))));
            }

            selectedProgram = nbt.getInteger(TAG_SELECTED);
            validateSelectedPage();
        }

        /**
         * Store the data to the specified NBT tag.
         *
         * @param nbt the tag to save the data to.
         */
        public void writeToNBT(final NBTTagCompound nbt) {
            final NBTTagList pagesNbt = new NBTTagList();
            int removed = 0;
            for (int index = 0; index < programs.size(); index++) {
                final List<String> program = programs.get(index);
                if (program.size() > 1 || program.get(0).length() > 0) {
                    pagesNbt.appendTag(new NBTTagString(String.join("\n", program)));
                } else if (index < selectedProgram) {
                    removed++;
                }
            }
            nbt.setTag(TAG_PAGES, pagesNbt);

            nbt.setInteger(TAG_SELECTED, selectedProgram - removed);
        }

        // --------------------------------------------------------------------- //

        private void validateSelectedPage() {
            selectedProgram = Math.max(0, Math.min(programs.size() - 1, selectedProgram));
        }

        // --------------------------------------------------------------------- //

        /**
         * Load code book data from the specified NBT tag.
         *
         * @param nbt the tag to load the data from.
         * @return the data loaded from the tag.
         */
        public static Data loadFromNBT(@Nullable final NBTTagCompound nbt) {
            final Data data = new Data();
            if (nbt != null) {
                data.readFromNBT(nbt);
            }
            return data;
        }

        /**
         * Load code book data from the specified item stack.
         *
         * @param stack the item stack to load the data from.
         * @return the data loaded from the stack.
         */
        public static Data loadFromStack(final ItemStack stack) {
            return loadFromNBT(stack.getTagCompound());
        }

        /**
         * Save the specified code book data to the specified item stack.
         *
         * @param stack the item stack to save the data to.
         * @param data  the data to save to the item stack.
         */
        public static void saveToStack(final ItemStack stack, final Data data) {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt == null) {
                stack.setTagCompound(nbt = new NBTTagCompound());
            }
            data.writeToNBT(nbt);
        }
    }
}
