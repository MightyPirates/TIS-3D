package li.cil.tis3d.common.item;

import li.cil.tis3d.api.ManualAPI;
import li.cil.tis3d.common.Settings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.List;

/**
 * Class for the Lightweight Module item, which is special
 */
public class ItemModuleLightweight extends ItemModule {

    private static final String TAG_MODULECLASS = "moduleClass";

    public static String getSelectedStrFromStack(ItemStack stack) {
        if (stack.hasTagCompound()) {
            return stack.getTagCompound().getString(TAG_MODULECLASS);
        }
        return "";
    }

    private void putSelectedStrIntoStack(ItemStack stack, String customModule) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setString(TAG_MODULECLASS, customModule);
    }

    @Override
    public String getHighlightTip(ItemStack item, String displayName) {
        return getSelectedStrFromStack(item);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        String className = getSelectedStrFromStack(stack);

        String[] data = className.split("\\.");
        if (data.length > 0)
            className = data[data.length - 1];

        if (className.isEmpty())
            className = StatCollector.translateToLocal("tis3d.tooltip.customNoFunction");

        tooltip.add(StatCollector.translateToLocal("tis3d.tooltip.customCurrentFunction"));
        tooltip.add(className);
        tooltip.add(StatCollector.translateToLocal("tis3d.tooltip.customChangeFunction"));
    }

    /*
     * Server-side only, used during a right-click to work out where we are in the list
     */
    private int getSelectedNumFromStr(String moduleClass) {
        for (int i = 0; i < Settings.customModules.length; i++) {
            if (Settings.customModules[i].equals(moduleClass))
                return i;
        }
        return -1;
    }

    @Override
    public ItemStack onItemRightClick(final ItemStack stack, final World world, final EntityPlayer playerIn) {
        // Only server knows what customModules we have!!!
        if (!world.isRemote) {
            if (playerIn.isSneaking()) {
                if (Settings.customModules.length != 0) {
                    int selNum = getSelectedNumFromStr(getSelectedStrFromStack(stack));
                    selNum++;
                    selNum = selNum % Settings.customModules.length;
                    putSelectedStrIntoStack(stack, Settings.customModules[selNum]);
                    return stack;
                }
            }
        }
        return super.onItemRightClick(stack, world, playerIn);
    }
}
