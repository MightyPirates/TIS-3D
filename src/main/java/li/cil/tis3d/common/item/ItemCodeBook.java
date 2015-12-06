package li.cil.tis3d.common.item;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.system.module.ModuleExecution;
import li.cil.tis3d.system.module.execution.MachineState;
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
                        data.addPage(code);
                        Data.saveToStack(stack, data);
                    }
                } else {
                    if (data.getPageCount() > 0) {
                        final String code = data.getPageContent(data.getSelectedPage());
                        moduleExecution.compile(code, player);
                    }
                }
                return true;
            }
        }
        return super.onItemUse(stack, player, world, pos, side, hitX, hitY, hitZ);
    }

    public static boolean isCodeBook(final ItemStack stack) {
        return stack != null && stack.getItem() == GameRegistry.findItem(API.MOD_ID, li.cil.tis3d.Constants.NAME_ITEM_CODE_BOOK);
    }

    public static class Data {
        private final List<String> pages = new ArrayList<>();
        private int selectedPage = 0;

        public int getSelectedPage() {
            return selectedPage;
        }

        public void setSelectedPage(final int selectedPage) {
            this.selectedPage = selectedPage;
        }

        public int getPageCount() {
            return pages.size();
        }

        public String getPageContent(final int page) {
            return pages.get(page);
        }

        public void addPage(final String content) {
            pages.add(content);
        }

        public void setPage(final int page, final String content) {
            pages.set(page, content);
        }

        public void removePage(final int page) {
            pages.remove(page);
        }

        public void readFromNBT(final NBTTagCompound nbt) {
            pages.clear();

            final NBTTagList pagesNbt = nbt.getTagList("pages", Constants.NBT.TAG_STRING);
            for (int page = 0; page < pagesNbt.tagCount(); page++) {
                pages.add(pagesNbt.getStringTagAt(page));
            }
        }

        public void writeToNBT(final NBTTagCompound nbt) {
            final NBTTagList pagesNbt = new NBTTagList();
            for (final String page : pages) {
                pagesNbt.appendTag(new NBTTagString(page));
            }
            nbt.setTag("pages", pagesNbt);
        }

        public static Data loadFromStack(final ItemStack stack) {
            final Data data = new Data();
            if (stack.hasTagCompound()) {
                data.readFromNBT(stack.getTagCompound());
            }
            return data;
        }

        public static void saveToStack(final ItemStack stack, final Data data) {
            if (!stack.hasTagCompound()) {
                stack.setTagCompound(new NBTTagCompound());
            }
            data.writeToNBT(stack.getTagCompound());
        }
    }
}
