package li.cil.tis3d.client.gui;

import li.cil.tis3d.common.init.Items;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

/**
 * GUI handler for the client side - which is, for now, all we need.
 */
public final class GuiHandlerClient implements IGuiHandler {
    public final static int ID_GUI_BOOK_MANUAL = 1;
    public final static int ID_GUI_BOOK_CODE = 2;

    @Override
    @Nullable
    public Object getServerGuiElement(final int id, final EntityPlayer player, final World world, final int x, final int y, final int z) {
        return null;
    }

    @Override
    @Nullable
    public Object getClientGuiElement(final int id, final EntityPlayer player, final World world, final int x, final int y, final int z) {
        switch (id) {
            case ID_GUI_BOOK_MANUAL:
                return new GuiManual();
            case ID_GUI_BOOK_CODE:
                if (Items.isBookCode(player.getHeldItem(EnumHand.MAIN_HAND)) && player == Minecraft.getMinecraft().player) {
                    return new GuiBookCode(player);
                }
                break;
        }
        return null;
    }
}
