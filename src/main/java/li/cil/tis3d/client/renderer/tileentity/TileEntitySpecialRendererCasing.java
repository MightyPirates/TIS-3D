package li.cil.tis3d.client.renderer.tileentity;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.api.util.TransformUtil;
import li.cil.tis3d.client.renderer.TextureLoader;
import li.cil.tis3d.common.TIS3D;
import li.cil.tis3d.common.init.Items;
import li.cil.tis3d.common.tileentity.TileEntityCasing;
import li.cil.tis3d.common.tileentity.TileEntityController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
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
public final class TileEntitySpecialRendererCasing extends TileEntityRenderer<TileEntityCasing> {
    private final static Set<Class<?>> BLACKLIST = new HashSet<>();

    @Override
    public void func_199341_a(final TileEntityCasing casing, final double x, final double y, final double z, final float partialTicks, final int destroyStage) {
        final double dx = x + 0.5;
        final double dy = y + 0.5;
        final double dz = z + 0.5;

        GlStateManager.pushMatrix();
        GlStateManager.translate(dx, dy, dz);

        RenderHelper.disableStandardItemLighting();

        // Render all modules, adjust GL state to allow easily rendering an
        // overlay in (0, 0, 0) to (1, 1, 0).
        for (final Face face : Face.VALUES) {
            if (isRenderingBackFace(face, dx, dy, dz)) {
                continue;
            }

            GlStateManager.pushMatrix();
            GlStateManager.pushAttrib();

            setupMatrix(face);

            ensureSanity();
            Minecraft.getMinecraft().getTextureMapBlocks().setBlurMipmap(false, false);

            if (!isObserverHoldingKey() || !drawConfigOverlay(casing, face)) {
                drawModuleOverlay(casing, face, partialTicks);
            }

            Minecraft.getMinecraft().getTextureMapBlocks().restoreLastBlurMipmap();
            GlStateManager.popAttrib();
            GlStateManager.popMatrix();
        }

        RenderHelper.enableStandardItemLighting();

        GlStateManager.popMatrix();
    }

    private boolean isRenderingBackFace(final Face face, final double dx, final double dy, final double dz) {
        final EnumFacing facing = Face.toEnumFacing(face.getOpposite());
        final double dotProduct = facing.getXOffset() * dx + facing.getYOffset() * (dy - rendererDispatcher.entity.getEyeHeight()) + facing.getZOffset() * dz;
        return dotProduct < 0;
    }

    private void setupMatrix(final Face face) {
        switch (face) {
            case Y_NEG:
                GlStateManager.rotate(-90, 1, 0, 0);
                break;
            case Y_POS:
                GlStateManager.rotate(90, 1, 0, 0);
                break;
            case Z_NEG:
                GlStateManager.rotate(0, 0, 1, 0);
                break;
            case Z_POS:
                GlStateManager.rotate(180, 0, 1, 0);
                break;
            case X_NEG:
                GlStateManager.rotate(90, 0, 1, 0);
                break;
            case X_POS:
                GlStateManager.rotate(-90, 0, 1, 0);
                break;
        }

        GlStateManager.translate(0.5, 0.5, -0.505);
        GlStateManager.scale(-1, -1, 1);
    }

    private void ensureSanity() {
        GlStateManager.enableTexture2D();
        RenderUtil.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderUtil.ignoreLighting();
    }

    private boolean drawConfigOverlay(final TileEntityCasing casing, final Face face) {
        // Only bother rendering the overlay if the player is nearby.
        if (!isObserverKindaClose(casing)) {
            return false;
        }

        if (isObserverSneaking() && !casing.isLocked()) {
            final TextureAtlasSprite closedSprite;
            final TextureAtlasSprite openSprite;

            final Port lookingAtPort;
            final boolean isLookingAt = isObserverLookingAt(casing.getPos(), face);
            if (isLookingAt) {
                closedSprite = RenderUtil.getSprite(TextureLoader.LOCATION_OVERLAY_CASING_PORT_CLOSED);
                openSprite = RenderUtil.getSprite(TextureLoader.LOCATION_OVERLAY_CASING_PORT_OPEN);

                final RayTraceResult hit = rendererDispatcher.cameraHitResult;
                final BlockPos pos = hit.getBlockPos();
                final Vec3d uv = TransformUtil.hitToUV(face, hit.hitVec.subtract(new Vec3d(pos)));
                lookingAtPort = Port.fromUVQuadrant(uv);
            } else {
                closedSprite = RenderUtil.getSprite(TextureLoader.LOCATION_OVERLAY_CASING_PORT_CLOSED_SMALL);
                openSprite = null;

                lookingAtPort = null;
            }

            GlStateManager.pushMatrix();
            for (final Port port : Port.CLOCKWISE) {
                final boolean isClosed = casing.isReceivingPipeLocked(face, port);
                final TextureAtlasSprite sprite = isClosed ? closedSprite : openSprite;
                if (sprite != null) {
                    RenderUtil.drawQuad(sprite);
                }

                if (port == lookingAtPort) {
                    RenderUtil.drawQuad(RenderUtil.getSprite(TextureLoader.LOCATION_OVERLAY_CASING_PORT_HIGHLIGHT));
                }

                GlStateManager.translate(0.5f, 0.5f, 0.5f);
                GlStateManager.rotate(90, 0, 0, 1);
                GlStateManager.translate(-0.5f, -0.5f, -0.5f);
            }
            GlStateManager.popMatrix();

            return isLookingAt;
        } else {
            final TextureAtlasSprite sprite;
            if (casing.isLocked()) {
                sprite = RenderUtil.getSprite(TextureLoader.LOCATION_OVERLAY_CASING_LOCKED);
            } else {
                sprite = RenderUtil.getSprite(TextureLoader.LOCATION_OVERLAY_CASING_UNLOCKED);
            }

            RenderUtil.drawQuad(sprite);
        }

        return true;
    }

    private void drawModuleOverlay(final TileEntityCasing casing, final Face face, final float partialTicks) {
        final TextureAtlasSprite closedSprite = RenderUtil.getSprite(TextureLoader.LOCATION_OVERLAY_CASING_PORT_CLOSED_SMALL);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, -0.005);
        for (final Port port : Port.CLOCKWISE) {
            final boolean isClosed = casing.isReceivingPipeLocked(face, port);
            if (isClosed) {
                RenderUtil.drawQuad(closedSprite);
            }

            GlStateManager.translate(0.5f, 0.5f, 0.5f);
            GlStateManager.rotate(90, 0, 0, 1);
            GlStateManager.translate(-0.5f, -0.5f, -0.5f);
        }
        GlStateManager.popMatrix();

        final Module module = casing.getModule(face);
        if (module == null) {
            return;
        }
        if (BLACKLIST.contains(module.getClass())) {
            return;
        }

        final int brightness = getWorld().getCombinedLight(
                casing.getPosition().offset(Face.toEnumFacing(face)), 0);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, brightness % 65536, brightness / 65536);

        try {
            module.render(casing.isEnabled(), partialTicks);
        } catch (final Exception e) {
            BLACKLIST.add(module.getClass());
            TIS3D.getLog().error("A module threw an exception while rendering, won't render again!", e);
        }
    }

    private boolean isObserverKindaClose(final TileEntityCasing casing) {
        return rendererDispatcher.entity.getDistanceSqToCenter(casing.getPos()) < 16 * 16;
    }

    private boolean isObserverHoldingKey() {
        for (ItemStack stack : rendererDispatcher.entity.getHeldEquipment()) {
            if (Items.isKey(stack)) {
                return true;
            }
        }

        return false;
    }

    private boolean isObserverSneaking() {
        return rendererDispatcher.entity.isSneaking();
    }

    private boolean isObserverLookingAt(final BlockPos pos, final Face face) {
        final RayTraceResult hit = rendererDispatcher.cameraHitResult;
        return hit != null &&
                hit.typeOfHit == RayTraceResult.Type.BLOCK &&
                hit.sideHit != null &&
                Face.fromEnumFacing(hit.sideHit) == face &&
                hit.getBlockPos() != null &&
                Objects.equals(hit.getBlockPos(), pos);
    }
}
