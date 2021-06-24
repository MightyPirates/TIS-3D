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

    public TileEntitySpecialRendererCasing(final TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(final TileEntityCasing casing, final float partialTicks, final MatrixStack matrixStack,
                       final IRenderTypeBuffer bufferFactory, final int light, final int overlay) {
        matrixStack.push();
        matrixStack.translate(0.5, 0.5, 0.5);

        final RenderContext context = new RenderContext(renderDispatcher, matrixStack, bufferFactory, partialTicks, light, overlay);

        // Render all modules, adjust matrix stack to allow easily rendering an overlay in (0, 0, 0) to (1, 1, 0).
        for (final Face face : Face.VALUES) {
            if (isBackFace(casing.getPosition(), face)) {
                continue;
            }

            matrixStack.push();
            setupMatrix(face, matrixStack);

            if (!isObserverHoldingKey() || !drawConfigOverlay(context, casing, face)) {
                // Grab neighbor lighting for module rendering because the casing itself is opaque and hence fully dark.
                final BlockPos neighborPos = casing.getPos().offset(Face.toDirection(face));
                final int neighborLight = WorldRenderer.getCombinedLight(renderDispatcher.world, neighborPos);
                drawModuleOverlay(new RenderContext(context, neighborLight), casing, face);
            }

            matrixStack.pop();
        }

        matrixStack.pop();
    }

    private boolean isBackFace(final BlockPos position, final Face face) {
        final Vector3d cameraPosition = renderDispatcher.renderInfo.getProjectedView();
        final Vector3d blockCenter = Vector3d.copy(position).add(0.5, 0.5, 0.5);
        final Vector3d faceNormal = new Vector3d(Face.toDirection(face).toVector3f());
        final Vector3d faceCenter = blockCenter.add(faceNormal.scale(0.5));
        final Vector3d cameraToFaceCenter = faceCenter.subtract(cameraPosition);
        return faceNormal.dotProduct(cameraToFaceCenter) > 0;
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

        matrixStack.rotate(new Quaternion(axis, degree, true));
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
            final boolean isLookingAt = isObserverLookingAt(casing.getPos(), face);
            if (isLookingAt) {
                closedSprite = Textures.LOCATION_OVERLAY_CASING_PORT_CLOSED;
                openSprite = Textures.LOCATION_OVERLAY_CASING_PORT_OPEN;

                final RayTraceResult hit = renderDispatcher.cameraHitResult;
                assert hit.getType() == RayTraceResult.Type.BLOCK : "renderDispatcher.cameraHitResult.getType() is not of type BLOCK even though it was in isObserverLookingAt";
                assert hit instanceof BlockRayTraceResult : "renderDispatcher.cameraHitResult is not a BlockRayTraceResult even though it was in isObserverLookingAt";
                final BlockRayTraceResult blockHit = (BlockRayTraceResult) hit;
                final BlockPos pos = blockHit.getPos();
                final Vector3d uv = TransformUtil.hitToUV(face, blockHit.getHitVec().subtract(Vector3d.copy(pos)));
                lookingAtPort = Port.fromUVQuadrant(uv);
            } else {
                closedSprite = Textures.LOCATION_OVERLAY_CASING_PORT_CLOSED_SMALL;
                openSprite = null;

                lookingAtPort = null;
            }

            final MatrixStack matrixStack = context.getMatrixStack();
            matrixStack.push();
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
                matrixStack.rotate(new Quaternion(Vector3f.ZP, 90, true));
                matrixStack.translate(-0.5, -0.5, -0.5);
            }
            matrixStack.pop();

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
        matrixStack.push();
        for (final Port port : Port.CLOCKWISE) {
            final boolean isClosed = casing.isReceivingPipeLocked(face, port);
            if (isClosed) {
                context.drawAtlasSpriteUnlit(Textures.LOCATION_OVERLAY_CASING_PORT_CLOSED_SMALL);
            }

            matrixStack.translate(0.5, 0.5, 0.5);
            matrixStack.rotate(new Quaternion(Vector3f.ZP, 90, true));
            matrixStack.translate(-0.5, -0.5, -0.5);
        }
        matrixStack.pop();

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
        return casing.getPos().withinDistance(renderDispatcher.renderInfo.getProjectedView(), 16);
    }

    private boolean isObserverHoldingKey() {
        for (final ItemStack stack : renderDispatcher.renderInfo.getRenderViewEntity().getHeldEquipment()) {
            if (Items.is(stack, Items.KEY) || Items.is(stack, Items.KEY_CREATIVE)) {
                return true;
            }
        }

        return false;
    }

    private boolean isObserverSneaking() {
        return renderDispatcher.renderInfo.getRenderViewEntity().isSneaking();
    }

    private boolean isObserverLookingAt(final BlockPos pos, final Face face) {
        final RayTraceResult hit = renderDispatcher.cameraHitResult;
        if (!(hit instanceof BlockRayTraceResult)) {
            return false;
        }

        final BlockRayTraceResult blockHit = (BlockRayTraceResult) hit;
        if (Face.fromDirection(blockHit.getFace()) != face) {
            return false;
        }

        return Objects.equals(blockHit.getPos(), pos);
    }
}
