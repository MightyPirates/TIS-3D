package li.cil.tis3d.common.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
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
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The code book, utility book for coding ASM programs for execution modules.
 */
public final class ItemBookCode extends ItemBook {
    private static final String TOOLTIP_BOOK_CODE = "tis3d.tooltip.bookCode";

    public ItemBookCode() {
        setMaxStackSize(1);
    }

    // --------------------------------------------------------------------- //
    // Item

    @SideOnly(Side.CLIENT)
    @Override
    public FontRenderer getFontRenderer(final ItemStack stack) {
        return Minecraft.getMinecraft().fontRenderer;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(final ItemStack stack, final EntityPlayer player, final List tooltip, final boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        final String info = StatCollector.translateToLocal(TOOLTIP_BOOK_CODE);
        tooltip.addAll(getFontRenderer(stack).listFormattedStringToWidth(info, li.cil.tis3d.common.Constants.MAX_TOOLTIP_WIDTH));
    }

    @Override
    public ItemStack onItemRightClick(final ItemStack stack, final World world, final EntityPlayer player) {
        if (world.isRemote) {
            player.openGui(TIS3D.instance, GuiHandlerClient.ID_GUI_BOOK_CODE, world, 0, 0, 0);
        }
        return super.onItemRightClick(stack, world, player);
    }

    @Override
    public boolean doesSneakBypassUse(final World world, final int x, final int y, final int z, final EntityPlayer player) {
        return world.blockExists(x, y, z) && world.getTileEntity(x, y, z) instanceof Casing;
    }

    // --------------------------------------------------------------------- //
    // ItemBook

    @Override
    public boolean isItemTool(final ItemStack stack) {
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
        public static Data loadFromNBT(final NBTTagCompound nbt) {
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
            if (!stack.hasTagCompound()) {
                stack.setTagCompound(new NBTTagCompound());
            }
            data.writeToNBT(stack.getTagCompound());
        }
    }
}
