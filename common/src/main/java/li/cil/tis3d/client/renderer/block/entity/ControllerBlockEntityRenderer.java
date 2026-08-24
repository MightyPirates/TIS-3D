/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import li.cil.tis3d.common.block.entity.ControllerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Objects;

public final class ControllerBlockEntityRenderer implements BlockEntityRenderer<ControllerBlockEntity, ControllerRenderState> {
    private static final Vec3 LABEL_ATTACHMENT = new Vec3(0.5, 1.4, 0.5);

    public ControllerBlockEntityRenderer(final BlockEntityRendererProvider.Context context) {
    }

    @Override
    public ControllerRenderState createRenderState() {
        return new ControllerRenderState();
    }

    @Override
    public void extractRenderState(final ControllerBlockEntity controller, final ControllerRenderState state,
                                   final float partialTicks, final Vec3 cameraPosition,
                                   @Nullable final ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(controller, state, partialTicks, cameraPosition, crumbling);

        state.controllerState = controller.getState();
        state.distanceToCameraSq = cameraPosition.distanceToSqr(Vec3.atCenterOf(controller.getBlockPos()));

        final HitResult hit = Minecraft.getInstance().hitResult;
        state.isObserverLookingAt = hit instanceof final BlockHitResult blockHit
            && Objects.equals(blockHit.getBlockPos(), controller.getBlockPos());
    }

    @Override
    public void submit(final ControllerRenderState state, final PoseStack matrixStack,
                       final SubmitNodeCollector collector, final CameraRenderState camera) {
        if (state.controllerState == null || !state.controllerState.isError || !state.isObserverLookingAt) {
            return;
        }

        // submitNameTag handles the billboarding and background for us.
        collector.submitNameTag(matrixStack, LABEL_ATTACHMENT, 0, state.controllerState.message,
            true, LightTexture.pack(0xF, 0xF), state.distanceToCameraSq, camera);
    }
}
