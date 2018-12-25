package li.cil.tis3d.client.renderer.tileentity;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.api.util.TransformUtil;
import li.cil.tis3d.client.init.Textures;
import li.cil.tis3d.common.TIS3D;
import li.cil.tis3d.common.block.entity.TileEntityCasing;
import li.cil.tis3d.common.init.Items;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HitResult;
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
public final class TileEntitySpecialRendererCasing extends BlockEntityRenderer<TileEntityCasing> {
    private final static Set<Class<?>> BLACKLIST = new HashSet<>();

    @Override
    public void render(final TileEntityCasing casing, final double x, final double y, final double z, final float partialTicks, final int destroyStage) {
        final double dx = x + 0.5;
        final double dy = y + 0.5;
        final double dz = z + 0.5;

        GlStateManager.pushMatrix();
        GlStateManager.translated(dx, dy, dz);

        GuiLighting.disable();

        // Render all modules, adjust GL state to allow easily rendering an
        // overlay in (0, 0, 0) to (1, 1, 0).
        for (final Face face : Face.VALUES) {
            if (isRenderingBackFace(face, dx, dy, dz)) {
                continue;
            }

            GlStateManager.pushMatrix();
            GlStateManager.pushLightingAttributes();

            setupMatrix(face);

            ensureSanity();
            MinecraftClient.getInstance().getSpriteAtlas().pushFilter(false, false);

            if (!isObserverHoldingKey() || !drawConfigOverlay(casing, face)) {
                drawModuleOverlay(casing, face, partialTicks);
            }

            MinecraftClient.getInstance().getSpriteAtlas().popFilter();
            GlStateManager.popAttributes();
            GlStateManager.popMatrix();
        }

        GuiLighting.enable();

        GlStateManager.popMatrix();
    }

    private boolean isRenderingBackFace(final Face face, final double dx, final double dy, final double dz) {
        final Direction facing = Face.toEnumFacing(face.getOpposite());
        final double dotProduct = facing.getOffsetX() * dx + facing.getOffsetY() * (dy - renderManager.cameraEntity.getEyeHeight()) + facing.getOffsetZ() * dz;
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

    private boolean drawConfigOverlay(final TileEntityCasing casing, final Face face) {
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

                final HitResult hit = renderManager.hitResult;
                final BlockPos pos = hit.getBlockPos();
                assert pos != null : "renderManager.hitResult.getBlockPos() is null even though it was non-null in isObserverLookingAt";
                final Vec3d uv = TransformUtil.hitToUV(face, hit.pos.subtract(new Vec3d(pos)));
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

    private void drawModuleOverlay(final TileEntityCasing casing, final Face face, final float partialTicks) {
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

        final int brightness = getWorld().getLightmapIndex(
            casing.getPosition().offset(Face.toEnumFacing(face)), 0);
        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, brightness % 65536, brightness / 65536);

        try {
            module.render(renderManager, partialTicks);
        } catch (final Exception e) {
            BLACKLIST.add(module.getClass());
            TIS3D.getLog().error("A module threw an exception while rendering, won't render again!", e);
        }
    }

    private boolean isObserverKindaClose(final TileEntityCasing casing) {
        return renderManager.cameraEntity.squaredDistanceToCenter(casing.getPos()) < 16 * 16;
    }

    private boolean isObserverHoldingKey() {
        for (final ItemStack stack : renderManager.cameraEntity.getItemsEquipped()) {
            if (Items.isKey(stack)) {
                return true;
            }
        }

        return false;
    }

    private boolean isObserverSneaking() {
        return renderManager.cameraEntity.isSneaking();
    }

    private boolean isObserverLookingAt(final BlockPos pos, final Face face) {
        final HitResult hit = renderManager.hitResult;
        return hit != null &&
            hit.type == HitResult.Type.BLOCK &&
            hit.side != null &&
            Face.fromEnumFacing(hit.side) == face &&
            hit.getBlockPos() != null &&
            Objects.equals(hit.getBlockPos(), pos);
    }
}
