package li.cil.tis3d.common.item;

import cpw.mods.fml.common.registry.GameRegistry;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.client.gui.GuiHandlerClient;
import li.cil.tis3d.common.TIS3D;
import li.cil.tis3d.common.module.ModuleExecution;
import li.cil.tis3d.common.module.execution.MachineState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
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
    public static final String TOOLTIP_BOOK_CODE = "tis3d.tooltip.bookCode";

    public ItemBookCode() {
        setMaxStackSize(1);
    }

    @Override
    public boolean isItemTool(final ItemStack stack) {
        return false;
    }

    @Override
    public int getItemEnchantability() {
        return 0;
    }

    @Override
    public void addInformation(final ItemStack stack, final EntityPlayer player, final List tooltip, final boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(StatCollector.translateToLocal(TOOLTIP_BOOK_CODE));
    }

    @Override
    public boolean onItemUse(final ItemStack stack, final EntityPlayer player, final World world, final int x, final int y, final int z, final int side, final float hitX, final float hitY, final float hitZ) {
        final TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof Casing) {
            final Casing casing = (Casing) tileEntity;
            final Module module = casing.getModule(Face.fromEnumFacing(EnumFacing.getFront(side)));
            if (module instanceof ModuleExecution) {
                final ModuleExecution moduleExecution = (ModuleExecution) module;
                final MachineState state = moduleExecution.getState();
                final Data data = Data.loadFromStack(stack);
                if (player.isSneaking()) {
                    if (state.code != null && state.code.length > 0) {
                        data.addProgram(Arrays.asList(state.code));
                        Data.saveToStack(stack, data);
                    }
                } else {
                    if (data.getProgramCount() > 0) {
                        final List<String> code = data.getProgram(data.getSelectedProgram());
                        moduleExecution.compile(code, player);
                    }
                }
                return true;
            }
        }
        return super.onItemUse(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
    }

    @Override
    public ItemStack onItemRightClick(final ItemStack stack, final World world, final EntityPlayer player) {
        if (world.isRemote) {
            player.openGui(TIS3D.instance, GuiHandlerClient.ID_GUI_BOOK_CODE, world, 0, 0, 0);
        }
        return super.onItemRightClick(stack, world, player);
    }

    public static boolean isBookCode(final ItemStack stack) {
        return stack != null && stack.getItem() == GameRegistry.findItem(API.MOD_ID, li.cil.tis3d.common.Constants.NAME_ITEM_BOOK_CODE);
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
