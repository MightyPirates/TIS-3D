package li.cil.tis3d.client.render.block.entity;

import li.cil.tis3d.common.block.entity.ControllerBlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public final class ControllerBlockEntityRenderer extends BlockEntityRenderer<ControllerBlockEntity> {
    public ControllerBlockEntityRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(final ControllerBlockEntity controller, float partialTicks, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        final ControllerBlockEntity.ControllerState state = controller.getState();
        if (!state.isError) {
            return;
        }

        final HitResult hitResult = dispatcher.crosshairTarget;
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

        //~ disableLightmap(true);
        //~ renderName(controller, I18n.translate(state.translateKey), x, y, z, 12);
        //~ disableLightmap(false);
    }
}
