package li.cil.tis3d.client.renderer.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.util.RenderContext;
import li.cil.tis3d.api.util.TransformUtil;
import li.cil.tis3d.client.renderer.Textures;
import li.cil.tis3d.common.item.Items;
import li.cil.tis3d.common.tileentity.TileEntityCasing;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Tile entity renderer for casings, used to dynamically render stuff for
 * different modules (in particular to allow dynamic displayed content, but
 * also so as not to spam the model registry with potentially a gazillion
 * block states for static individual texturing).
 */
public final class TileEntitySpecialRendererCasing extends TileEntityRenderer<TileEntityCasing> {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final double Z_FIGHT_BUFFER = 0.001;
    private final static Set<Class<?>> BLACKLIST = new HashSet<>();

    public TileEntitySpecialRendererCasing(final TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    public void render(final TileEntityCasing casing, final float partialTicks, final MatrixStack matrixStack,
                       final IRenderTypeBuffer bufferFactory, final int light, final int overlay) {
        matrixStack.pushPose();
        matrixStack.translate(0.5, 0.5, 0.5);

        final RenderContext context = new RenderContext(renderer, matrixStack, bufferFactory, partialTicks, light, overlay);

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
                final int neighborLight = WorldRenderer.getLightColor(renderer.level, neighborPos);
                drawModuleOverlay(new RenderContext(context, neighborLight), casing, face);
            }

            matrixStack.popPose();
        }

        matrixStack.popPose();
    }

    private boolean isBackFace(final BlockPos position, final Face face) {
        final Vector3d cameraPosition = renderer.camera.getPosition();
        final Vector3d blockCenter = Vector3d.atCenterOf(position);
        final Vector3d faceNormal = Vector3d.atLowerCornerOf(Face.toDirection(face).getNormal());
        final Vector3d faceCenter = blockCenter.add(faceNormal.scale(0.5));
        final Vector3d cameraToFaceCenter = faceCenter.subtract(cameraPosition);
        return faceNormal.dot(cameraToFaceCenter) > 0;
    }

    private void setupMatrix(final Face face, final MatrixStack matrixStack) {
        final Vector3f axis;
        final int degree;

        switch (face) {
            case Y_NEG:
                axis = Vector3f.XP;
                degree = -90;
                break;
            case Y_POS:
                axis = Vector3f.XP;
                degree = 90;
                break;
            case Z_NEG:
                axis = Vector3f.YP;
                degree = 0;
                break;
            case Z_POS:
                axis = Vector3f.YP;
                degree = 180;
                break;
            case X_NEG:
                axis = Vector3f.YP;
                degree = 90;
                break;
            case X_POS:
                axis = Vector3f.YP;
                degree = -90;
                break;
            default:
                throw new IllegalArgumentException("Invalid face");
        }

        matrixStack.mulPose(new Quaternion(axis, degree, true));
        matrixStack.translate(0.5, 0.5, -(0.5 + Z_FIGHT_BUFFER));
        matrixStack.scale(-1, -1, 1);
    }

    private boolean drawConfigOverlay(final RenderContext context, final TileEntityCasing casing, final Face face) {
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

                final RayTraceResult hit = renderer.cameraHitResult;
                assert hit.getType() == RayTraceResult.Type.BLOCK : "renderer.cameraHitResult.getType() is not of type BLOCK even though it was in isObserverLookingAt";
                assert hit instanceof BlockRayTraceResult : "renderer.cameraHitResult is not a BlockRayTraceResult even though it was in isObserverLookingAt";
                final BlockRayTraceResult blockHit = (BlockRayTraceResult) hit;
                final BlockPos pos = blockHit.getBlockPos();
                final Vector3d uv = TransformUtil.hitToUV(face, blockHit.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ()));
                lookingAtPort = Port.fromUVQuadrant(uv);
            } else {
                closedSprite = Textures.LOCATION_OVERLAY_CASING_PORT_CLOSED_SMALL;
                openSprite = null;

                lookingAtPort = null;
            }

            final MatrixStack matrixStack = context.getMatrixStack();
            matrixStack.pushPose();
            for (final Port port : Port.CLOCKWISE) {
                final boolean isClosed = casing.isReceivingPipeLocked(face, port);
                final ResourceLocation sprite = isClosed ? closedSprite : openSprite;
                if (sprite != null) {
                    context.drawAtlasSpriteUnlit(sprite);
                }

                if (port == lookingAtPort) {
                    context.drawAtlasSpriteUnlit(Textures.LOCATION_OVERLAY_CASING_PORT_HIGHLIGHT);
                }

                matrixStack.translate(0.5, 0.5, 0.5);
                matrixStack.mulPose(new Quaternion(Vector3f.ZP, 90, true));
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

            context.drawAtlasSpriteUnlit(sprite);
        }

        return true;
    }

    private void drawModuleOverlay(final RenderContext context, final TileEntityCasing casing, final Face face) {
        final MatrixStack matrixStack = context.getMatrixStack();
        matrixStack.pushPose();
        for (final Port port : Port.CLOCKWISE) {
            final boolean isClosed = casing.isReceivingPipeLocked(face, port);
            if (isClosed) {
                context.drawAtlasSpriteUnlit(Textures.LOCATION_OVERLAY_CASING_PORT_CLOSED_SMALL);
            }

            matrixStack.translate(0.5, 0.5, 0.5);
            matrixStack.mulPose(new Quaternion(Vector3f.ZP, 90, true));
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

    private boolean isObserverKindaClose(final TileEntityCasing casing) {
        return casing.getBlockPos().closerThan(renderer.camera.getPosition(), 16);
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
        final RayTraceResult hit = renderer.cameraHitResult;
        if (!(hit instanceof BlockRayTraceResult)) {
            return false;
        }

        final BlockRayTraceResult blockHit = (BlockRayTraceResult) hit;
        if (Face.fromDirection(blockHit.getDirection()) != face) {
            return false;
        }

        return Objects.equals(blockHit.getBlockPos(), pos);
    }
}
