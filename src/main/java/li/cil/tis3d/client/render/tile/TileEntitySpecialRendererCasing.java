package li.cil.tis3d.client.render.tile;

import cpw.mods.fml.common.registry.GameRegistry;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.client.render.TextureLoader;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.TIS3D;
import li.cil.tis3d.common.tile.TileEntityCasing;
import li.cil.tis3d.util.OneEightCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;

import java.util.HashSet;
import java.util.Set;

/**
 * Tile entity renderer for casings, used to dynamically render stuff for
 * different modules (in particular to allow dynamic displayed content, but
 * also so as not to spam the model registry with potentially a gazillion
 * block states for static individual texturing).
 */
public final class TileEntitySpecialRendererCasing extends TileEntitySpecialRenderer {
    private final static Set<Class<?>> BLACKLIST = new HashSet<>();
    private static Item key;

    @Override
    public void renderTileEntityAt(final TileEntity tileEntity, final double x, final double y, final double z, final float partialTicks) {
        final TileEntityCasing casing = (TileEntityCasing) tileEntity;

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

        RenderHelper.disableStandardItemLighting();

        // Render all modules, adjust GL state to allow easily rendering an
        // overlay in (0, 0, 0) to (1, 1, 0).
        for (final Face face : Face.VALUES) {
            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

            setupMatrix(face);

            ensureSanity(casing, face);

            if (isPlayerHoldingKey()) {
                drawLockOverlay(casing);
            } else {
                drawModuleOverlay(casing, face, partialTicks);
            }

            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }

        RenderHelper.enableStandardItemLighting();

        GL11.glPopMatrix();
    }

    private void setupMatrix(final Face face) {
        switch (face) {
            case Y_NEG:
                GL11.glRotatef(-90, 1, 0, 0);
                break;
            case Y_POS:
                GL11.glRotatef(90, 1, 0, 0);
                break;
            case Z_NEG:
                GL11.glRotatef(0, 0, 1, 0);
                break;
            case Z_POS:
                GL11.glRotatef(180, 0, 1, 0);
                break;
            case X_NEG:
                GL11.glRotatef(90, 0, 1, 0);
                break;
            case X_POS:
                GL11.glRotatef(-90, 0, 1, 0);
                break;
        }

        GL11.glTranslated(0.5, 0.5, -0.505);
        GL11.glScalef(-1, -1, 1);
    }

    private void ensureSanity(final TileEntityCasing casing, final Face face) {
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        final EnumFacing facing = Face.toEnumFacing(face);
        final int neighborX = casing.getPositionX() + facing.getFrontOffsetX();
        final int neighborY = casing.getPositionY() + facing.getFrontOffsetY();
        final int neighborZ = casing.getPositionZ() + facing.getFrontOffsetZ();
        final int brightness = casing.getWorldObj().getLightBrightnessForSkyBlocks(neighborX, neighborY, neighborZ, 0);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, brightness % 65536, brightness / 65536);

        GL11.glColor4f(1, 1, 1, 1);
    }

    private void drawLockOverlay(final TileEntityCasing casing) {
        // Only bother rendering the overlay if the player is nearby.
        if (!isPlayerKindaClose(casing)) {
            return;
        }

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 0);

        RenderUtil.bindTexture(TextureMap.locationBlocksTexture);

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
        return OneEightCompat.getDistanceSqToCenter(player, casing.xCoord, casing.yCoord, casing.zCoord) < 16 * 16;
    }

    private boolean isPlayerHoldingKey() {
        // Cache the key item reference to avoid having to query the game
        // registry every rendered frame.
        if (key == null) {
            key = GameRegistry.findItem(API.MOD_ID, Constants.NAME_ITEM_KEY);
        }

        final EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        final ItemStack stack = player.getHeldItem();
        return stack != null && stack.getItem() == key;
    }
}
