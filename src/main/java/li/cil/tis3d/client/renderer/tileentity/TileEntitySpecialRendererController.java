package li.cil.tis3d.client.renderer.tileentity;

import li.cil.tis3d.common.block.entity.TileEntityController;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.HitResult;

public final class TileEntitySpecialRendererController extends BlockEntityRenderer<TileEntityController> {
    @Override
    public void render(final TileEntityController controller, final double x, final double y, final double z, final float partialTicks, final int destroyStage) {
        final TileEntityController.ControllerState state = controller.getState();
        if (!state.isError) {
            return;
        }

        final HitResult hit = renderManager.hitResult;
        if (hit != null &&
            hit.type == HitResult.Type.BLOCK &&
            hit.getBlockPos() != null &&
            hit.getBlockPos().equals(controller.getPos())) {
            method_3570(true);
            method_3567(controller, I18n.translate(state.translateKey), x, y, z, 12);
            method_3570(false);
        }
    }
}
