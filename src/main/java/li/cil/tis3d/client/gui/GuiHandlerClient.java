package li.cil.tis3d.client.gui;

import cpw.mods.fml.common.network.IGuiHandler;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.common.init.Items;
import li.cil.tis3d.common.module.ModuleTerminal;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * GUI handler for the client side - which is, still, all we need.
 */
public final class GuiHandlerClient implements IGuiHandler {
    public enum GuiId {
        BOOK_MANUAL,
        BOOK_CODE,
        MODULE_TERMINAL;

        public static final GuiId[] VALUES = values();
    }

    @Override
    @Nullable
    public Object getServerGuiElement(final int id, final EntityPlayer player, final World world, final int x, final int y, final int z) {
        return null;
    }

    @Override
    @Nullable
    public Object getClientGuiElement(final int id, final EntityPlayer player, final World world, final int x, final int y, final int z) {
        switch (GuiId.VALUES[id]) {
            case BOOK_CODE:
                return getGuiBookCode(player);
            case BOOK_MANUAL:
                return new GuiManual();
            case MODULE_TERMINAL:
                return getGuiModuleTerminal(world);
        }
        return null;
    }

    @Nullable
    private Object getGuiModuleTerminal(final World world) {
        final MovingObjectPosition hit = Minecraft.getMinecraft().objectMouseOver;
        if (hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            return null;
        }

        final TileEntity tileEntity = world.getTileEntity(hit.blockX, hit.blockY, hit.blockZ);
        if (!(tileEntity instanceof Casing)) {
            return null;
        }

        final Casing casing = (Casing) tileEntity;
        final Module module = casing.getModule(Face.fromIntFacing(hit.sideHit));
        if (!(module instanceof ModuleTerminal)) {
            return null;
        }

        return new GuiModuleTerminal((ModuleTerminal) module);
    }

    @Nullable
    private Object getGuiBookCode(final EntityPlayer player) {
        if (!Items.isBookCode(player.getHeldItem())) {
            return null;
        }

        return new GuiBookCode(player);
    }
}
