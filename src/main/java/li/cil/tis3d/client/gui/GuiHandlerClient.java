package li.cil.tis3d.client.gui;

import li.cil.tis3d.common.item.ItemCodeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

/**
 * GUI handler for the client side - which is, for now, all we need.
 */
public final class GuiHandlerClient implements IGuiHandler {
    public final static int ID_GUI_MANUAL = 1;
    public final static int ID_GUI_CODE_BOOK = 2;

    @Override
    public Object getServerGuiElement(final int id, final EntityPlayer player, final World world, final int x, final int y, final int z) {
        return null;
    }

    @Override
    public Object getClientGuiElement(final int id, final EntityPlayer player, final World world, final int x, final int y, final int z) {
        switch (id) {
            case ID_GUI_MANUAL:
                return new GuiManual();
            case ID_GUI_CODE_BOOK:
                if (ItemCodeBook.isCodeBook(player.getHeldItem()) && player == Minecraft.getMinecraft().thePlayer) {
                    return new GuiCodeBook(player);
                }
                break;
        }
        return null;
    }
}
