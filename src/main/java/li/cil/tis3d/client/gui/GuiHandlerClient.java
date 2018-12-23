package li.cil.tis3d.client.gui;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.common.gui.GuiHandlerCommon;
import li.cil.tis3d.common.init.Items;
import li.cil.tis3d.common.module.ModuleTerminal;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.HitResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * GUI handler for the client side - which is, still, all we need.
 */
public final class GuiHandlerClient extends GuiHandlerCommon {
    public static Gui getClientGuiElement(GuiId id, World world, PlayerEntity player) {
        switch (id) {
            case BOOK_CODE:
                return getGuiBookCode(player);
            case BOOK_MANUAL:
                return new GuiManual();
            case MODULE_TERMINAL:
                return getGuiModuleTerminal(world);
            case MODULE_MEMORY:
                return getGuiModuleMemory(player);
        }
        return null;
    }

    // --------------------------------------------------------------------- //

    @Nullable
    private static Gui getGuiBookCode(final PlayerEntity player) {
        if (!Items.isBookCode(player.getStackInHand(Hand.MAIN))) {
            return null;
        }

        return new GuiBookCode(player);
    }

    @Nullable
    private static Gui getGuiModuleTerminal(final World world) {
        final HitResult hit = MinecraftClient.getInstance().hitResult;
        if (hit == null || hit.type != HitResult.Type.BLOCK) {
            return null;
        }

        final BlockEntity tileEntity = world.getBlockEntity(hit.getBlockPos());
        if (!(tileEntity instanceof Casing)) {
            return null;
        }

        final Casing casing = (Casing) tileEntity;
        final Module module = casing.getModule(Face.fromEnumFacing(hit.side));
        if (!(module instanceof ModuleTerminal)) {
            return null;
        }

        return new GuiModuleTerminal((ModuleTerminal) module);
    }

    @Nullable
    private static Gui getGuiModuleMemory(final PlayerEntity player) {
        if (!Items.isModuleReadOnlyMemory(player.getStackInHand(Hand.MAIN))) {
            return null;
        }

        return new GuiModuleMemory(player);
    }
}
