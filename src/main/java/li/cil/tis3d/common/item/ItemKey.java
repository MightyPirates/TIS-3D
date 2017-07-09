package li.cil.tis3d.common.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.common.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

/**
 * Base item for all keys.
 */
public final class ItemKey extends Item {
    public ItemKey() {
        setMaxStackSize(1);
    }

    // --------------------------------------------------------------------- //
    // Item

    @Override
    public boolean getShareTag() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(final ItemStack stack, final EntityPlayer player, final List tooltip, final boolean extended) {
        super.addInformation(stack, player, tooltip, extended);
        final String info = I18n.format(Constants.TOOLTIP_KEY);
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        tooltip.addAll(fontRenderer.listFormattedStringToWidth(info, li.cil.tis3d.common.Constants.MAX_TOOLTIP_WIDTH));
    }

    @Override
    public boolean doesSneakBypassUse(final World world, final int x, final int y, final int z, final EntityPlayer player) {
        return world.getTileEntity(x, y, z) instanceof Casing;
    }

    @Override
    public boolean isItemTool(final ItemStack stack) {
        return false;
    }
}
