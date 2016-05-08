package li.cil.tis3d.client.render.tile;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.client.render.TextureLoader;
import li.cil.tis3d.common.TIS3D;
import li.cil.tis3d.common.init.Items;
import li.cil.tis3d.common.tile.TileEntityCasing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;

import java.util.HashSet;
import java.util.Set;

/**
 * Tile entity renderer for casings, used to dynamically render stuff for
 * different modules (in particular to allow dynamic displayed content, but
 * also so as not to spam the model registry with potentially a gazillion
 * block states for static individual texturing).
 */
public final class TileEntitySpecialRendererCasing extends TileEntitySpecialRenderer<TileEntityCasing> {
    private final static Set<Class<?>> BLACKLIST = new HashSet<>();

    @Override
    public void renderTileEntityAt(final TileEntityCasing casing, final double x, final double y, final double z, final float partialTicks, final int destroyStage) {
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

            ensureSanity(casing, face);

            if (isPlayerHoldingKey()) {
                drawLockOverlay(casing);
            } else {
                drawModuleOverlay(casing, face, partialTicks);
            }

            GlStateManager.popAttrib();
            GlStateManager.popMatrix();
        }

        RenderHelper.enableStandardItemLighting();

        GlStateManager.popMatrix();
    }

    private boolean isRenderingBackFace(final Face face, final double dx, final double dy, final double dz) {
        final EnumFacing facing = Face.toEnumFacing(face.getOpposite());
        final double dotProduct = facing.getFrontOffsetX() * dx + facing.getFrontOffsetY() * (dy - Minecraft.getMinecraft().thePlayer.getEyeHeight()) + facing.getFrontOffsetZ() * dz;
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

    private void ensureSanity(final TileEntityCasing casing, final Face face) {
        GlStateManager.enableTexture2D();

        final int brightness = getWorld().getCombinedLight(
                casing.getPosition().offset(Face.toEnumFacing(face)), 0);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, brightness % 65536, brightness / 65536);

        GlStateManager.color(1, 1, 1, 1);
    }

    private void drawLockOverlay(final TileEntityCasing casing) {
        // Only bother rendering the overlay if the player is nearby.
        if (!isPlayerKindaClose(casing)) {
            return;
        }

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 0);

        RenderUtil.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        final TextureAtlasSprite icon;
        if (casing.isLocked()) {
            icon = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(TextureLoader.LOCATION_CASING_LOCKED_OVERLAY.toString());
        } else {
            icon = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(TextureLoader.LOCATION_CASING_UNLOCKED_OVERLAY.toString());
        }

        RenderUtil.drawQuad(icon.getMinU(), icon.getMinV(), icon.getMaxU(), icon.getMaxV());
    }

    private void drawModuleOverlay(final TileEntityCasing casing, final Face face, final float partialTicks) {
        final Module module = casing.getModule(face);
        if (module == null) {
            return;
        }
        if (BLACKLIST.contains(module.getClass())) {
            return;
        }

        try {
            module.render(casing.isEnabled(), partialTicks);
        } catch (final Exception e) {
            BLACKLIST.add(module.getClass());
            TIS3D.getLog().error("A module threw an exception while rendering, won't render again!", e);
        }
    }

    private boolean isPlayerKindaClose(final TileEntityCasing casing) {
        final EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        return player.getDistanceSqToCenter(casing.getPos()) < 16 * 16;
    }

    private boolean isPlayerHoldingKey() {
        final EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        return Items.isKey(player.getHeldItem(EnumHand.MAIN_HAND));
    }
}
