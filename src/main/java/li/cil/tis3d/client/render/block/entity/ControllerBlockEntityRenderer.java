package li.cil.tis3d.client.render.block.entity;

import li.cil.tis3d.common.block.entity.ControllerBlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.BlockHitResult;
import net.minecraft.util.HitResult;

public final class ControllerBlockEntityRenderer extends BlockEntityRenderer<ControllerBlockEntity> {
    @Override
    public void render(final ControllerBlockEntity controller, final double x, final double y, final double z, final float partialTicks, final int destroyStage) {
        final ControllerBlockEntity.ControllerState state = controller.getState();
        if (!state.isError) {
            return;
        }

        final HitResult hitResult = renderManager.hitResult;
        if (hitResult == null) {
            return;
        }
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        final BlockHitResult blockHitResult = (BlockHitResult)hitResult;
        if (blockHitResult.getBlockPos() == null) {
            return;
        }
        if (!blockHitResult.getBlockPos().equals(controller.getPos())) {
            return;
        }

        method_3570(true);
        method_3567(controller, I18n.translate(state.translateKey), x, y, z, 12);
        method_3570(false);
    }
}
