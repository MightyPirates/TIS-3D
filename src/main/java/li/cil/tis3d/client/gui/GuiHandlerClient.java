package li.cil.tis3d.client.gui;

import cpw.mods.fml.common.network.IGuiHandler;
import li.cil.tis3d.common.init.Items;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/**
 * GUI handler for the client side - which is, for now, all we need.
 */
public final class GuiHandlerClient implements IGuiHandler {
    public final static int ID_GUI_BOOK_MANUAL = 1;
    public final static int ID_GUI_BOOK_CODE = 2;

    @Override
    public Object getServerGuiElement(final int id, final EntityPlayer player, final World world, final int x, final int y, final int z) {
        return null;
    }

    @Override
    public Object getClientGuiElement(final int id, final EntityPlayer player, final World world, final int x, final int y, final int z) {
        switch (id) {
            case ID_GUI_BOOK_MANUAL:
                return new GuiManual();
            case ID_GUI_BOOK_CODE:
                if (Items.isBookCode(player.getHeldItem()) && player == Minecraft.getMinecraft().thePlayer) {
                    return new GuiBookCode(player);
                }
                break;
        }
        return null;
    }
}
