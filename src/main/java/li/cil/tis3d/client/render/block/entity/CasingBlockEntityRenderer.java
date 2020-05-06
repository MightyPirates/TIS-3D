package li.cil.tis3d.client.render.block.entity;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.api.util.TransformUtil;
import li.cil.tis3d.client.init.Textures;
import li.cil.tis3d.common.TIS3D;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.init.Items;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Tile entity renderer for casings, used to dynamically render stuff for
 * different modules (in particular to allow dynamic displayed content, but
 * also so as not to spam the model registry with potentially a gazillion
 * block states for static individual texturing).
 */
public final class CasingBlockEntityRenderer extends BlockEntityRenderer<CasingBlockEntity> {
    private final static Set<Class<?>> BLACKLIST = new HashSet<>();

    public CasingBlockEntityRenderer(BlockEntityRenderDispatcher dispatcher) {
		super(dispatcher);
	}

    @Override
    public void render(final CasingBlockEntity casing, float partialTicks, final MatrixStack matrices,
                       final VertexConsumerProvider vertexConsumers, final int light, final int overlay) {
        matrices.push();
        matrices.translate(0.5, 0.5, 0.5);

        // Render all modules, adjust GL state to allow easily rendering an
        // overlay in (0, 0, 0) to (1, 1, 0).
        for (final Face face : Face.VALUES) {
            // Fixme: This already wasn't working in 1.14, let's leave it for later
            //~ if (isRenderingBackFace(face, dx, dy, dz)) {
                //~ continue;
            //~ }

            matrices.push();
            setupMatrix(face, matrices);

            if (!isObserverHoldingKey() || !drawConfigOverlay(casing, face, matrices, vertexConsumers, overlay)) {
                // XXX light is not the correct lightmap value
                drawModuleOverlay(casing, face, partialTicks, matrices, vertexConsumers, light, overlay);
            }

            matrices.pop();
        }

        matrices.pop();
    }

    private boolean isRenderingBackFace(final Face face, final double dx, final double dy, final double dz) {
        final Direction facing = Face.toDirection(face.getOpposite());
        final double dotProduct = facing.getOffsetX() * dx + facing.getOffsetY() * (dy - dispatcher.camera.getFocusedEntity().getEyeHeight(dispatcher.camera.getFocusedEntity().getPose())) + facing.getOffsetZ() * dz;
        return dotProduct < 0;
    }

    private void setupMatrix(final Face face, final MatrixStack matrices) {
        final Vector3f axis;
        final int degree;

        switch (face) {
            case Y_NEG:
                axis = Vector3f.POSITIVE_X;
                degree = -90;
                break;
            case Y_POS:
                axis = Vector3f.POSITIVE_X;
                degree = 90;
                break;
            case Z_NEG:
                axis = Vector3f.POSITIVE_Y;
                degree = 0;
                break;
            case Z_POS:
                axis = Vector3f.POSITIVE_Y;
                degree = 180;
                break;
            case X_NEG:
                axis = Vector3f.POSITIVE_Y;
                degree = 90;
                break;
            case X_POS:
                axis = Vector3f.POSITIVE_Y;
                degree = -90;
                break;
            default:
                axis = null;
                throw new RuntimeException("Invalid face");
        }

        matrices.multiply(new Quaternion(axis, degree, true));
        matrices.translate(0.5f, 0.5f, -0.505f);
        matrices.scale(-1, -1, 1);
    }

    private boolean drawConfigOverlay(final CasingBlockEntity casing, final Face face,
                                      final MatrixStack matrices, final VertexConsumerProvider vcp,
                                      final int overlay) {
        // Only bother rendering the overlay if the player is nearby.
        if (!isObserverKindaClose(casing)) {
            return false;
        }

        final VertexConsumer vc = vcp.getBuffer(TexturedRenderLayers.getEntityCutout());

        if (isObserverSneaking() && !casing.isLocked()) {
            final Sprite closedSprite;
            final Sprite openSprite;

            final Port lookingAtPort;
            final boolean isLookingAt = isObserverLookingAt(casing.getPos(), face);
            if (isLookingAt) {
                closedSprite = RenderUtil.getSprite(Textures.LOCATION_OVERLAY_CASING_PORT_CLOSED);
                openSprite = RenderUtil.getSprite(Textures.LOCATION_OVERLAY_CASING_PORT_OPEN);

                final HitResult hitResult = dispatcher.crosshairTarget;
                assert hitResult.getType() == HitResult.Type.BLOCK : "dispatcher.hitResult.getBlockPos().getType() is not of type BLOCK even though it was in isObserverLookingAt";
                assert hitResult instanceof BlockHitResult : "dispatcher.hitResult.getBlockPos() is not a BlockHitResult even though it was in isObserverLookingAt";
                final BlockHitResult blockHitResult = (BlockHitResult)hitResult;
                final BlockPos pos = blockHitResult.getBlockPos();
                assert pos != null : "dispatcher.hitResult.getBlockPos() is null even though it was non-null in isObserverLookingAt";
                final Vec3d uv = TransformUtil.hitToUV(face, hitResult.getPos().subtract(new Vec3d(pos)));
                lookingAtPort = Port.fromUVQuadrant(uv);
            } else {
                closedSprite = RenderUtil.getSprite(Textures.LOCATION_OVERLAY_CASING_PORT_CLOSED_SMALL);
                openSprite = null;

                lookingAtPort = null;
            }

            matrices.push();
            for (final Port port : Port.CLOCKWISE) {
                final boolean isClosed = casing.isReceivingPipeLocked(face, port);
                final Sprite sprite = isClosed ? closedSprite : openSprite;
                if (sprite != null) {
                    RenderUtil.drawQuad(sprite, matrices.peek(), vc, RenderUtil.maxLight, overlay);
                }

                if (port == lookingAtPort) {
                    final Sprite highlightSprite = RenderUtil.getSprite(Textures.LOCATION_OVERLAY_CASING_PORT_HIGHLIGHT);
                    RenderUtil.drawQuad(highlightSprite, matrices.peek(), vc, RenderUtil.maxLight, overlay);
                }

                matrices.translate(0.5f, 0.5f, 0.5f);
                matrices.multiply(new Quaternion(Vector3f.POSITIVE_Z, 90, true));
                matrices.translate(-0.5f, -0.5f, -0.5f);
            }
            matrices.pop();

            return isLookingAt;
        } else {
            final Sprite sprite;
            if (casing.isLocked()) {
                sprite = RenderUtil.getSprite(Textures.LOCATION_OVERLAY_CASING_LOCKED);
            } else {
                sprite = RenderUtil.getSprite(Textures.LOCATION_OVERLAY_CASING_UNLOCKED);
            }

            RenderUtil.drawQuad(sprite, matrices.peek(), vc, RenderUtil.maxLight, overlay);
        }

        return true;
    }

    private void drawModuleOverlay(final CasingBlockEntity casing, final Face face, final float partialTicks,
                                   final MatrixStack matrices, final VertexConsumerProvider vcp,
                                   final int light, final int overlay) {
        final VertexConsumer vc = vcp.getBuffer(TexturedRenderLayers.getEntityCutout());
        final Sprite closedSprite = RenderUtil.getSprite(Textures.LOCATION_OVERLAY_CASING_PORT_CLOSED_SMALL);

        matrices.push();
        matrices.translate(0, 0, -0.005f);
        for (final Port port : Port.CLOCKWISE) {
            final boolean isClosed = casing.isReceivingPipeLocked(face, port);
            if (isClosed) {
                RenderUtil.drawQuad(closedSprite, matrices.peek(), vc, RenderUtil.maxLight, overlay);
            }

            matrices.translate(0.5f, 0.5f, 0.5f);
            matrices.multiply(new Quaternion(Vector3f.POSITIVE_Z, 90, true));
            matrices.translate(-0.5f, -0.5f, -0.5f);
        }
        matrices.pop();

        final Module module = casing.getModule(face);
        if (module == null) {
            return;
        }
        if (BLACKLIST.contains(module.getClass())) {
            return;
        }

        try {
            module.render(dispatcher, partialTicks, matrices, vcp, light, overlay);
        } catch (final Exception e) {
            BLACKLIST.add(module.getClass());
            TIS3D.getLog().error("A module threw an exception while rendering, won't render again!", e);
        }
    }

    private boolean isObserverKindaClose(final CasingBlockEntity casing) {
        return dispatcher.camera.getBlockPos().getSquaredDistance(casing.getPos()) < 16 * 16;
    }

    private boolean isObserverHoldingKey() {
        for (final ItemStack stack : dispatcher.camera.getFocusedEntity().getItemsEquipped()) {
            if (Items.isKey(stack)) {
                return true;
            }
        }

        return false;
    }

    private boolean isObserverSneaking() {
        return dispatcher.camera.getFocusedEntity().isSneaking();
    }

    private boolean isObserverLookingAt(final BlockPos pos, final Face face) {
        final HitResult hitResult = dispatcher.crosshairTarget;
        if (hitResult == null) {
            return false;
        }
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return false;
        }

        final BlockHitResult blockHitResult = (BlockHitResult)hitResult;
        if (blockHitResult.getSide() == null) {
            return false;
        }
        if (Face.fromDirection(blockHitResult.getSide()) != face) {
            return false;
        }
        if (blockHitResult.getBlockPos() == null) {
            return false;
        }

        return Objects.equals(blockHitResult.getBlockPos(), pos);
    }
}
