package li.cil.tis3d.common;

import li.cil.tis3d.Constants;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Our creative tab! There are many like it, but this one is... kind of the same. Oh well.
 */
public final class CreativeTab extends CreativeTabs {
    public CreativeTab() {
        super("TIS-3D");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Item getTabIconItem() {
        return GameRegistry.findItem(Constants.MOD_ID, Constants.NAME_BLOCK_CONTROLLER);
    }
}
