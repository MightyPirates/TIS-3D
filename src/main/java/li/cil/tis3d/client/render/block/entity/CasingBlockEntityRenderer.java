package li.cil.tis3d.client.render.block.entity;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
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
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
//~ import net.minecraft.client.util.math.Vector4f;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

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
    //~ private final Vector4f translationExtractor = new Vector4f();

    public CasingBlockEntityRenderer(BlockEntityRenderDispatcher dispatcher) {
		super(dispatcher);
	}

    @Override
    public void render(final CasingBlockEntity casing, float partialTicks, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        //~ translationExtractor.set(0, 0, 0, 1);
        //~ translationExtractor.transform(matrices.peek().getModel());
        //~ final double dx = -(translationExtractor.getX());
        //~ final double dy = -(translationExtractor.getY()-2);
        //~ final double dz = -(translationExtractor.getZ());

        //~ final double dx = x + 0.5;
        //~ final double dy = y + 0.5;
        //~ final double dz = z + 0.5;

        GlStateManager.pushMatrix();
        //~ GlStateManager.translated(dx, dy, dz);

        DiffuseLighting.disable();

        // Render all modules, adjust GL state to allow easily rendering an
        // overlay in (0, 0, 0) to (1, 1, 0).
        for (final Face face : Face.VALUES) {
            // Fixme: This already wasn't working in 1.14, let's leave it for later
            //~ if (isRenderingBackFace(face, dx, dy, dz)) {
                //~ continue;
            //~ }

            GlStateManager.pushMatrix();
            GlStateManager.pushLightingAttributes();

            setupMatrix(face);

            ensureSanity();
            Identifier dummyIden = new Identifier("minecraft", "dirt"); // XXX
            //~ MinecraftClient.getInstance().getSpriteAtlas(dummyIden).pushFilter(false, false); // XXX

            if (!isObserverHoldingKey() || !drawConfigOverlay(casing, face)) {
                drawModuleOverlay(casing, face, partialTicks);
            }

            //~ MinecraftClient.getInstance().getSpriteAtlas(dummyIden).popFilter(); // XXX
            GlStateManager.popAttributes();
            GlStateManager.popMatrix();
        }

        //~ DiffuseLighting.enableForLevel();

        GlStateManager.popMatrix();
    }

    private boolean isRenderingBackFace(final Face face, final double dx, final double dy, final double dz) {
        final Direction facing = Face.toDirection(face.getOpposite());
        final double dotProduct = facing.getOffsetX() * dx + facing.getOffsetY() * (dy - dispatcher.camera.getFocusedEntity().getEyeHeight(dispatcher.camera.getFocusedEntity().getPose())) + facing.getOffsetZ() * dz;
        return dotProduct < 0;
    }

    private void setupMatrix(final Face face) {
        switch (face) {
            case Y_NEG:
                GlStateManager.rotatef(-90, 1, 0, 0);
                break;
            case Y_POS:
                GlStateManager.rotatef(90, 1, 0, 0);
                break;
            case Z_NEG:
                GlStateManager.rotatef(0, 0, 1, 0);
                break;
            case Z_POS:
                GlStateManager.rotatef(180, 0, 1, 0);
                break;
            case X_NEG:
                GlStateManager.rotatef(90, 0, 1, 0);
                break;
            case X_POS:
                GlStateManager.rotatef(-90, 0, 1, 0);
                break;
        }

        GlStateManager.translated(0.5, 0.5, -0.505);
        GlStateManager.scaled(-1, -1, 1);
    }

    private void ensureSanity() {
        GlStateManager.enableTexture();
        RenderUtil.bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX);
        GlStateManager.color4f(1, 1, 1, 1);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderUtil.ignoreLighting();
    }

    private boolean drawConfigOverlay(final CasingBlockEntity casing, final Face face) {
        // Only bother rendering the overlay if the player is nearby.
        if (!isObserverKindaClose(casing)) {
            return false;
        }

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

            GlStateManager.pushMatrix();
            for (final Port port : Port.CLOCKWISE) {
                final boolean isClosed = casing.isReceivingPipeLocked(face, port);
                final Sprite sprite = isClosed ? closedSprite : openSprite;
                if (sprite != null) {
                    RenderUtil.drawQuad(sprite);
                }

                if (port == lookingAtPort) {
                    RenderUtil.drawQuad(RenderUtil.getSprite(Textures.LOCATION_OVERLAY_CASING_PORT_HIGHLIGHT));
                }

                GlStateManager.translatef(0.5f, 0.5f, 0.5f);
                GlStateManager.rotatef(90, 0, 0, 1);
                GlStateManager.translatef(-0.5f, -0.5f, -0.5f);
            }
            GlStateManager.popMatrix();

            return isLookingAt;
        } else {
            final Sprite sprite;
            if (casing.isLocked()) {
                sprite = RenderUtil.getSprite(Textures.LOCATION_OVERLAY_CASING_LOCKED);
            } else {
                sprite = RenderUtil.getSprite(Textures.LOCATION_OVERLAY_CASING_UNLOCKED);
            }

            RenderUtil.drawQuad(sprite);
        }

        return true;
    }

    private void drawModuleOverlay(final CasingBlockEntity casing, final Face face, final float partialTicks) {
        final Sprite closedSprite = RenderUtil.getSprite(Textures.LOCATION_OVERLAY_CASING_PORT_CLOSED_SMALL);

        GlStateManager.pushMatrix();
        GlStateManager.translated(0, 0, -0.005);
        for (final Port port : Port.CLOCKWISE) {
            final boolean isClosed = casing.isReceivingPipeLocked(face, port);
            if (isClosed) {
                RenderUtil.drawQuad(closedSprite);
            }

            GlStateManager.translatef(0.5f, 0.5f, 0.5f);
            GlStateManager.rotatef(90, 0, 0, 1);
            GlStateManager.translatef(-0.5f, -0.5f, -0.5f);
        }
        GlStateManager.popMatrix();

        final Module module = casing.getModule(face);
        if (module == null) {
            return;
        }
        if (BLACKLIST.contains(module.getClass())) {
            return;
        }

        //~ final int brightness = getWorld().getLightmapIndex(
            //~ casing.getPosition().offset(Face.toDirection(face)), 0);
        //~ GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, brightness % 65536, (float)(brightness / 65536));
        final int brightness = 0; // XXX

        try {
            module.render(dispatcher, partialTicks);
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
