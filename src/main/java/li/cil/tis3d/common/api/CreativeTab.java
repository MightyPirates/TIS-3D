package li.cil.tis3d.common.api;

import li.cil.tis3d.api.API;
import li.cil.tis3d.common.init.Blocks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Our creative tab! There are many like it, but this one is... kind of the same. Oh well.
 */
public final class CreativeTab extends CreativeTabs {
    public CreativeTab() {
        super(API.MOD_ID);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ItemStack getTabIconItem() {
        return new ItemStack(Item.getItemFromBlock(Blocks.controller));
    }
}
