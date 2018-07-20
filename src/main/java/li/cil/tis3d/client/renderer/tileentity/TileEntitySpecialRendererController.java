package li.cil.tis3d.client.renderer.tileentity;

import li.cil.tis3d.common.tileentity.TileEntityController;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.RayTraceResult;

public class TileEntitySpecialRendererController extends TileEntitySpecialRenderer<TileEntityController> {
    @Override
    public void renderTileEntityAt(final TileEntityController controller, final double x, final double y, final double z, final float partialTicks, final int destroyStage) {
        final TileEntityController.ControllerState state = controller.getState();
        if (!state.isError) {
            return;
        }

        RayTraceResult hitResult = rendererDispatcher.cameraHitResult;
        if (hitResult != null &&
                hitResult.typeOfHit == RayTraceResult.Type.BLOCK &&
                hitResult.getBlockPos() != null &&
                hitResult.getBlockPos().equals(controller.getPos())) {
            setLightmapDisabled(true);
            drawNameplate(controller, I18n.format(state.translateKey), x, y, z, 12);
            setLightmapDisabled(false);
        }
    }
}
