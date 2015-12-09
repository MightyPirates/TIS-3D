package li.cil.tis3d.common.item;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.common.module.ModuleExecution;
import li.cil.tis3d.common.module.execution.MachineState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * The code book, utility book for coding ASM programs for execution modules.
 */
public final class ItemCodeBook extends ItemBook {
    public ItemCodeBook() {
        setMaxStackSize(1);
    }

    @Override
    public ItemStack onItemRightClick(final ItemStack stack, final World world, final EntityPlayer player) {
        player.openGui(Loader.instance().getIndexedModList().get(API.MOD_ID).getMod(), 0, world, 0, 0, 0);
        return super.onItemRightClick(stack, world, player);
    }

    @Override
    public boolean onItemUse(final ItemStack stack, final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        final TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof Casing) {
            final Casing casing = (Casing) tileEntity;
            final Module module = casing.getModule(Face.fromEnumFacing(side));
            if (module instanceof ModuleExecution) {
                final ModuleExecution moduleExecution = (ModuleExecution) module;
                final MachineState state = moduleExecution.getState();
                final Data data = Data.loadFromStack(stack);
                if (player.isSneaking()) {
                    if (state.code != null && state.code.length > 0) {
                        final String code = String.join("\n", state.code);
                        data.addProgram(code);
                        Data.saveToStack(stack, data);
                    }
                } else {
                    if (data.getProgramCount() > 0) {
                        final String code = data.getProgram(data.getSelectedProgram());
                        moduleExecution.compile(code, player);
                    }
                }
                return true;
            }
        }
        return super.onItemUse(stack, player, world, pos, side, hitX, hitY, hitZ);
    }

    public static boolean isCodeBook(final ItemStack stack) {
        return stack != null && stack.getItem() == GameRegistry.findItem(API.MOD_ID, li.cil.tis3d.common.Constants.NAME_ITEM_CODE_BOOK);
    }

    // --------------------------------------------------------------------- //

    /**
     * Wrapper for list of programs stored in the code book.
     */
    public static class Data {
        private static final String TAG_PAGES = "pages";

        private final List<String> programs = new ArrayList<>();
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
            this.selectedProgram = Math.max(0, Math.min(programs.size(), index));
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
        public String getProgram(final int index) {
            return programs.get(index);
        }

        /**
         * Add a new program to the book.
         *
         * @param code the code of the program to add.
         */
        public void addProgram(final String code) {
            programs.add(code);
        }

        /**
         * Overwrite a program at the specified index.
         *
         * @param page the index of the program to overwrite.
         * @param code the code of the program.
         */
        public void setProgram(final int page, final String code) {
            programs.set(page, code);
        }

        /**
         * Remove a program from the book.
         *
         * @param index the index of the program to remove.
         */
        public void removeProgram(final int index) {
            programs.remove(index);
        }

        /**
         * Load data from the specified NBT tag.
         *
         * @param nbt the tag to load the data from.
         */
        public void readFromNBT(final NBTTagCompound nbt) {
            programs.clear();

            final NBTTagList pagesNbt = nbt.getTagList(TAG_PAGES, Constants.NBT.TAG_STRING);
            for (int page = 0; page < pagesNbt.tagCount(); page++) {
                programs.add(pagesNbt.getStringTagAt(page));
            }
        }

        /**
         * Store the data to the specified NBT tag.
         *
         * @param nbt the tag to save the data to.
         */
        public void writeToNBT(final NBTTagCompound nbt) {
            final NBTTagList pagesNbt = new NBTTagList();
            for (final String page : programs) {
                pagesNbt.appendTag(new NBTTagString(page));
            }
            nbt.setTag(TAG_PAGES, pagesNbt);
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
