package li.cil.tis3d.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.util.RenderContext;
import li.cil.tis3d.api.util.TransformUtil;
import li.cil.tis3d.client.renderer.RenderContextImpl;
import li.cil.tis3d.client.renderer.Textures;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.item.Items;
import li.cil.tis3d.common.network.Network;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Tile entity renderer for casings, used to dynamically render stuff for
 * different modules (in particular to allow dynamic displayed content, but
 * also so as not to spam the model registry with potentially a gazillion
 * block states for static individual texturing).
 */
public final class CasingBlockEntityRenderer implements BlockEntityRenderer<CasingBlockEntity> {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final double Z_FIGHT_BUFFER = 0.001;
    private static final Vector3f AXIS_X_POSITIVE = new Vector3f(1, 0, 0);
    private static final Vector3f AXIS_Y_POSITIVE = new Vector3f(0, 1, 0);
    private static final Vector3f AXIS_Z_POSITIVE = new Vector3f(0, 0, 1);
    private final static Set<Class<?>> BLACKLIST = new HashSet<>();

    private final BlockEntityRenderDispatcher renderer;

    public CasingBlockEntityRenderer(final BlockEntityRendererProvider.Context context) {
        renderer = context.getBlockEntityRenderDispatcher();
    }

    @Override
    public int getViewDistance() {
        return Network.RANGE_HIGH;
    }

    @Override
    public void render(final CasingBlockEntity casing, final float partialTicks, final PoseStack matrixStack,
                       final MultiBufferSource bufferFactory, final int light, final int overlay) {
        matrixStack.pushPose();
        matrixStack.translate(0.5, 0.5, 0.5);

        final RenderContextImpl context = new RenderContextImpl(renderer, matrixStack, bufferFactory, partialTicks, light, overlay);

        // Render all modules, adjust matrix stack to allow easily rendering an overlay in (0, 0, 0) to (1, 1, 0).
        for (final Face face : Face.VALUES) {
            if (isBackFace(casing.getPosition(), face)) {
                continue;
            }

            matrixStack.pushPose();
            setupMatrix(face, matrixStack);

            if (!isObserverHoldingKey() || !drawConfigOverlay(context, casing, face)) {
                // Grab neighbor lighting for module rendering because the casing itself is opaque and hence fully dark.
                final BlockPos neighborPos = casing.getBlockPos().relative(Face.toDirection(face));
                final int neighborLight = LevelRenderer.getLightColor(renderer.level, neighborPos);
                drawModuleOverlay(new RenderContextImpl(context, neighborLight), casing, face);
            }

            matrixStack.popPose();
        }

        matrixStack.popPose();
    }

    private boolean isBackFace(final BlockPos position, final Face face) {
        final Vec3 cameraPosition = renderer.camera.getPosition();
        final Vec3 blockCenter = Vec3.atCenterOf(position);
        final Vec3 faceNormal = Vec3.atLowerCornerOf(Face.toDirection(face).getNormal());
        final Vec3 faceCenter = blockCenter.add(faceNormal.scale(0.5));
        final Vec3 cameraToFaceCenter = faceCenter.subtract(cameraPosition);
        return faceNormal.dot(cameraToFaceCenter) > 0;
    }

    private void setupMatrix(final Face face, final PoseStack matrixStack) {
        final Vector3f axis;
        final int degree;

        switch (face) {
            case Y_NEG -> {
                axis = AXIS_X_POSITIVE;
                degree = -90;
            }
            case Y_POS -> {
                axis = AXIS_X_POSITIVE;
                degree = 90;
            }
            case Z_NEG -> {
                axis = AXIS_Y_POSITIVE;
                degree = 0;
            }
            case Z_POS -> {
                axis = AXIS_Y_POSITIVE;
                degree = 180;
            }
            case X_NEG -> {
                axis = AXIS_Y_POSITIVE;
                degree = 90;
            }
            case X_POS -> {
                axis = AXIS_Y_POSITIVE;
                degree = -90;
            }
            default -> throw new IllegalArgumentException("Invalid face");
        }

        matrixStack.mulPose(new Quaternionf().fromAxisAngleDeg(axis, degree));
        matrixStack.translate(0.5, 0.5, -(0.5 + Z_FIGHT_BUFFER));
        matrixStack.scale(-1, -1, 1);
    }

    private boolean drawConfigOverlay(final RenderContext context, final CasingBlockEntity casing, final Face face) {
        // Only bother rendering the overlay if the player is nearby.
        if (!isObserverKindaClose(casing)) {
            return false;
        }

        if (isObserverSneaking() && !casing.isLocked()) {
            final ResourceLocation closedSprite;
            final ResourceLocation openSprite;

            final Port lookingAtPort;
            final boolean isLookingAt = isObserverLookingAt(casing.getPosition(), face);
            if (isLookingAt) {
                closedSprite = Textures.LOCATION_OVERLAY_CASING_PORT_CLOSED;
                openSprite = Textures.LOCATION_OVERLAY_CASING_PORT_OPEN;

                final HitResult hit = renderer.cameraHitResult;
                assert hit.getType() == HitResult.Type.BLOCK : "renderer.cameraHitResult.getType() is not of type BLOCK even though it was in isObserverLookingAt";
                assert hit instanceof BlockHitResult : "renderer.cameraHitResult is not a BlockRayTraceResult even though it was in isObserverLookingAt";
                final BlockHitResult blockHit = (BlockHitResult) hit;
                final BlockPos pos = blockHit.getBlockPos();
                final Vec3 uv = TransformUtil.hitToUV(face, blockHit.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ()));
                lookingAtPort = Port.fromUVQuadrant(uv);
            } else {
                closedSprite = Textures.LOCATION_OVERLAY_CASING_PORT_CLOSED_SMALL;
                openSprite = null;

                lookingAtPort = null;
            }

            final PoseStack matrixStack = context.getMatrixStack();
            matrixStack.pushPose();
            for (final Port port : Port.CLOCKWISE) {
                final boolean isClosed = casing.isReceivingPipeLocked(face, port);
                final ResourceLocation sprite = isClosed ? closedSprite : openSprite;
                if (sprite != null) {
                    context.drawAtlasQuadUnlit(sprite);
                }

                if (port == lookingAtPort) {
                    context.drawAtlasQuadUnlit(Textures.LOCATION_OVERLAY_CASING_PORT_HIGHLIGHT);
                }

                matrixStack.translate(0.5, 0.5, 0.5);
                matrixStack.mulPose(new Quaternionf().fromAxisAngleDeg(AXIS_Z_POSITIVE, 90));
                matrixStack.translate(-0.5, -0.5, -0.5);
            }
            matrixStack.popPose();

            return isLookingAt;
        } else {
            final ResourceLocation sprite;
            if (casing.isLocked()) {
                sprite = Textures.LOCATION_OVERLAY_CASING_LOCKED;
            } else {
                sprite = Textures.LOCATION_OVERLAY_CASING_UNLOCKED;
            }

            context.drawAtlasQuadUnlit(sprite);
        }

        return true;
    }

    private void drawModuleOverlay(final RenderContext context, final CasingBlockEntity casing, final Face face) {
        final PoseStack matrixStack = context.getMatrixStack();
        matrixStack.pushPose();
        for (final Port port : Port.CLOCKWISE) {
            final boolean isClosed = casing.isReceivingPipeLocked(face, port);
            if (isClosed) {
                context.drawAtlasQuadUnlit(Textures.LOCATION_OVERLAY_CASING_PORT_CLOSED_SMALL);
            }

            matrixStack.translate(0.5, 0.5, 0.5);
            matrixStack.mulPose(new Quaternionf().fromAxisAngleDeg(AXIS_Z_POSITIVE, 90));
            matrixStack.translate(-0.5, -0.5, -0.5);
        }
        matrixStack.popPose();

        final Module module = casing.getModule(face);
        if (module == null) {
            return;
        }
        if (BLACKLIST.contains(module.getClass())) {
            return;
        }

        try {
            module.render(context);
        } catch (final Exception e) {
            BLACKLIST.add(module.getClass());
            LOGGER.error("A module threw an exception while rendering, won't render again!", e);
        }
    }

    private boolean isObserverKindaClose(final CasingBlockEntity casing) {
        return casing.getBlockPos().closerToCenterThan(renderer.camera.getPosition(), 16);
    }

    private boolean isObserverHoldingKey() {
        for (final ItemStack stack : renderer.camera.getEntity().getHandSlots()) {
            if (Items.is(stack, Items.KEY) || Items.is(stack, Items.KEY_CREATIVE)) {
                return true;
            }
        }

        return false;
    }

    private boolean isObserverSneaking() {
        return renderer.camera.getEntity().isShiftKeyDown();
    }

    private boolean isObserverLookingAt(final BlockPos pos, final Face face) {
        final HitResult hit = renderer.cameraHitResult;
        if (!(hit instanceof final BlockHitResult blockHit)) {
            return false;
        }

        if (Face.fromDirection(blockHit.getDirection()) != face) {
            return false;
        }

        return Objects.equals(blockHit.getBlockPos(), pos);
    }
}
