package li.cil.tis3d.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.util.RenderContext;
import li.cil.tis3d.api.util.TransformUtil;
import li.cil.tis3d.client.renderer.RenderContextImpl;
import li.cil.tis3d.client.renderer.SubmitBufferSource;
import li.cil.tis3d.client.renderer.Textures;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.item.Items;
import li.cil.tis3d.common.network.Network;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Tile entity renderer for casings, used to dynamically render stuff for
 * different modules (in particular to allow dynamic displayed content, but
 * also so as not to spam the model registry with potentially a gazillion
 * block states for static individual texturing).
 */
public final class CasingBlockEntityRenderer implements BlockEntityRenderer<CasingBlockEntity, CasingRenderState> {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final double Z_FIGHT_BUFFER = 0.001;
    private static final Vector3f AXIS_X_POSITIVE = new Vector3f(1, 0, 0);
    private static final Vector3f AXIS_Y_POSITIVE = new Vector3f(0, 1, 0);
    private static final Vector3f AXIS_Z_POSITIVE = new Vector3f(0, 0, 1);
    private final static Set<Class<?>> BLACKLIST = new HashSet<>();

    private final SubmitBufferSource bufferSource = new SubmitBufferSource();

    public CasingBlockEntityRenderer(final BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return Network.RANGE_HIGH;
    }

    @Override
    public CasingRenderState createRenderState() {
        return new CasingRenderState();
    }

    @Override
    public void extractRenderState(final CasingBlockEntity casing, final CasingRenderState state,
                                   final float partialTicks, final Vec3 cameraPosition,
                                   @Nullable final ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(casing, state, partialTicks, cameraPosition, crumbling);

        final Minecraft minecraft = Minecraft.getInstance();

        state.casing = casing;
        state.partialTicks = partialTicks;
        state.cameraPosition = cameraPosition;
        state.cameraHitResult = minecraft.hitResult;

        // Grab neighbor lighting for module rendering because the casing itself is opaque and hence fully dark.
        if (casing.getLevel() != null) {
            for (final Face face : Face.VALUES) {
                final BlockPos neighborPos = casing.getBlockPos().relative(Face.toDirection(face));
                state.neighborLight[face.ordinal()] = LevelRenderer.getLightColor(casing.getLevel(), neighborPos);
            }
        }

        state.observerHoldingKey = isObserverHoldingKey(minecraft);
        state.observerSneaking = minecraft.player != null && minecraft.player.isShiftKeyDown();
    }

    @Override
    public void submit(final CasingRenderState state, final PoseStack matrixStack,
                       final SubmitNodeCollector collector, final CameraRenderState camera) {
        final CasingBlockEntity casing = state.casing;
        if (casing == null) {
            return;
        }

        matrixStack.pushPose();
        matrixStack.translate(0.5, 0.5, 0.5);

        final RenderContextImpl context = new RenderContextImpl(matrixStack, bufferSource,
            state.cameraPosition, state.cameraHitResult, state.partialTicks, state.lightCoords, OverlayTexture.NO_OVERLAY);

        // Render all modules, adjust matrix stack to allow easily rendering an overlay in (0, 0, 0) to (1, 1, 0).
        for (final Face face : Face.VALUES) {
            if (isBackFace(state.cameraPosition, casing.getPosition(), face)) {
                continue;
            }

            matrixStack.pushPose();
            setupMatrix(face, matrixStack);

            if (!state.observerHoldingKey || !drawConfigOverlay(context, state, casing, face)) {
                drawModuleOverlay(new RenderContextImpl(context, state.neighborLight[face.ordinal()]), casing, face);
            }

            matrixStack.popPose();
        }

        matrixStack.popPose();

        bufferSource.submitTo(collector, matrixStack);
    }

    private boolean isBackFace(final Vec3 cameraPosition, final BlockPos position, final Face face) {
        final Vec3 blockCenter = Vec3.atCenterOf(position);
        final Vec3 faceNormal = Vec3.atLowerCornerOf(Face.toDirection(face).getUnitVec3i());
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

    private boolean drawConfigOverlay(final RenderContext context, final CasingRenderState state,
                                      final CasingBlockEntity casing, final Face face) {
        // Only bother rendering the overlay if the player is nearby.
        if (!casing.getBlockPos().closerToCenterThan(state.cameraPosition, 16)) {
            return false;
        }

        if (state.observerSneaking && !casing.isLocked()) {
            final Identifier closedSprite;
            final Identifier openSprite;

            final Port lookingAtPort;
            final boolean isLookingAt = isObserverLookingAt(state.cameraHitResult, casing.getPosition(), face);
            if (isLookingAt) {
                closedSprite = Textures.LOCATION_OVERLAY_CASING_PORT_CLOSED;
                openSprite = Textures.LOCATION_OVERLAY_CASING_PORT_OPEN;

                final BlockHitResult blockHit = (BlockHitResult) Objects.requireNonNull(state.cameraHitResult);
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
                final Identifier sprite = isClosed ? closedSprite : openSprite;
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
            final Identifier sprite;
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

        matrixStack.pushPose();
        try {
            module.render(context);
        } catch (final Exception e) {
            BLACKLIST.add(module.getClass());
            LOGGER.error("A module threw an exception while rendering, won't render again!", e);
        } finally {
            matrixStack.popPose();
        }
    }

    private static boolean isObserverHoldingKey(final Minecraft minecraft) {
        if (!(minecraft.getCameraEntity() instanceof final LivingEntity observer)) {
            return false;
        }

        for (final InteractionHand hand : InteractionHand.values()) {
            final ItemStack stack = observer.getItemInHand(hand);
            if (Items.is(stack, Items.KEY) || Items.is(stack, Items.KEY_CREATIVE)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isObserverLookingAt(@Nullable final HitResult hit, final BlockPos pos, final Face face) {
        if (!(hit instanceof final BlockHitResult blockHit)) {
            return false;
        }

        if (Face.fromDirection(blockHit.getDirection()) != face) {
            return false;
        }

        return Objects.equals(blockHit.getBlockPos(), pos);
    }
}
