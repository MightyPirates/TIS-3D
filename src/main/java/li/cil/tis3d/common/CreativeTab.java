package li.cil.tis3d.common;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import li.cil.tis3d.Constants;
import li.cil.tis3d.api.API;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

/**
 * Our creative tab! There are many like it, but this one is... kind of the same. Oh well.
 */
public final class CreativeTab extends CreativeTabs {
    public CreativeTab() {
        super(API.MOD_ID);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Item getTabIconItem() {
        return GameRegistry.findItem(API.MOD_ID, Constants.NAME_BLOCK_CONTROLLER);
    }
}
