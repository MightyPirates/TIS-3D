package li.cil.tis3d.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import li.cil.tis3d.common.block.entity.ControllerBlockEntity;
import li.cil.tis3d.util.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Objects;

public final class ControllerBlockEntityRenderer implements BlockEntityRenderer<ControllerBlockEntity> {
    private final BlockEntityRenderDispatcher renderer;
    private final Font font;

    public ControllerBlockEntityRenderer(final BlockEntityRendererProvider.Context context) {
        renderer = context.getBlockEntityRenderDispatcher();
        font = context.getFont();
    }

    @Override
    public void render(final ControllerBlockEntity controller, final float partialTicks, final PoseStack matrixStack, final MultiBufferSource bufferFactory, final int light, final int overlay) {
        final ControllerBlockEntity.ControllerState state = controller.getState();
        if (!state.isError) {
            return;
        }

        final HitResult hit = renderer.cameraHitResult;
        if (hit instanceof final BlockHitResult blockHit) {
            if (Objects.equals(blockHit.getBlockPos(), controller.getBlockPos())) {
                renderState(matrixStack, bufferFactory, state);
            }
        }
    }

    private void renderState(final PoseStack matrixStack, final MultiBufferSource bufferFactory, final ControllerBlockEntity.ControllerState state) {
        matrixStack.pushPose();
        matrixStack.translate(0.5, 1.4, 0.5);
        matrixStack.mulPose(renderer.camera.rotation());
        matrixStack.scale(-0.025f, -0.025f, 0.025f);

        final Component message = state.message;
        final float x = -font.width(message) / 2.0f;
        final var matrix = matrixStack.last().pose();
        final int backgroundColor = Minecraft.getInstance().options.getBackgroundColor(0.25f);
        final int maxBrightness = LightTexture.pack(0xF, 0xF);
        font.drawInBatch(message, x, 0, Color.withAlpha(Color.WHITE, 0.125f), false, matrix, bufferFactory, true, backgroundColor, maxBrightness);
        font.drawInBatch(message, x, 0, Color.WHITE, false, matrix, bufferFactory, false, 0, maxBrightness);

        matrixStack.popPose();
    }
}
