package li.cil.tis3d.client.render.block.entity;

import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.common.block.entity.ControllerBlockEntity;
import li.cil.tis3d.util.ColorUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Matrix4f;

@Environment(EnvType.CLIENT)
public final class ControllerBlockEntityRenderer implements BlockEntityRenderer<ControllerBlockEntity> {

    private BlockEntityRenderDispatcher dispatcher;
    private TextRenderer textRenderer;

    public ControllerBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super();
        dispatcher = context.getRenderDispatcher();
        textRenderer = context.getTextRenderer();
    }

    /**
     * Draw the controller state as a floating text label.
     * {@code drawLabel()} was removed in 1.15.
     *
     * @param state    the controller state.
     * @param matrices the transformation stack.
     * @param vcp      the buffer provider.
     */
    private void renderState(final ControllerBlockEntity.ControllerState state,
                             final MatrixStack matrices, final VertexConsumerProvider vcp) {
        final Camera camera = this.dispatcher.camera;
        // I would add a check for distance to camera here, but this seems to happen automatically (?)
        final String str = I18n.translate(state.translateKey);
        matrices.push();
        matrices.translate(0.5f, 1.4f, 0.5f);
        matrices.multiply(camera.getRotation());
        matrices.scale(-0.025f, -0.025f, 0.025f);

        final Matrix4f modMat = matrices.peek().getModel();
        final int bg = MinecraftClient.getInstance().options.getTextBackgroundColor(0.25f);
        final float x = -textRenderer.getWidth(str) / 2.0f;
        textRenderer.draw(str, x, 0, 0x20FFFFFF, false, modMat, vcp, true, bg, RenderUtil.maxLight);
        textRenderer.draw(str, x, 0, ColorUtils.WHITE, false, modMat, vcp, false, 0, RenderUtil.maxLight);

        matrices.pop();
    }

    @Override
    public void render(final ControllerBlockEntity controller, final float partialTicks, final MatrixStack matrices, final VertexConsumerProvider vertexConsumers, final int light, final int overlay) {
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

        renderState(state, matrices, vertexConsumers);
    }
}
