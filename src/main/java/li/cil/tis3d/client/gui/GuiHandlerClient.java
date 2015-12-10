package li.cil.tis3d.client.gui;

import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/**
 * GUI handler for the client side - which is, for now, all we need.
 */
public final class GuiHandlerClient implements IGuiHandler {
    public final static int ID_GUI_MANUAL = 1;

    @Override
    public Object getServerGuiElement(final int id, final EntityPlayer player, final World world, final int x, final int y, final int z) {
        return null;
    }

    @Override
    public Object getClientGuiElement(final int id, final EntityPlayer player, final World world, final int x, final int y, final int z) {
        switch (id) {
            case ID_GUI_MANUAL:
                return new GuiManual();
        }
        return null;
    }
}
