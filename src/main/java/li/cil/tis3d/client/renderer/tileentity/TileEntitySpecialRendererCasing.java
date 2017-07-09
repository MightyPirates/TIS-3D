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
import li.cil.tis3d.util.OneEightCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
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

    @Override
    public void renderTileEntityAt(final TileEntity tileEntity, final double x, final double y, final double z, final float partialTicks) {
        final TileEntityCasing casing = (TileEntityCasing) tileEntity;
        final double dx = x + 0.5;
        final double dy = y + 0.5;
        final double dz = z + 0.5;


        GL11.glPushMatrix();
        GL11.glTranslated(dx, dy, dz);

        RenderHelper.disableStandardItemLighting();

        // Render all modules, adjust GL state to allow easily rendering an
        // overlay in (0, 0, 0) to (1, 1, 0).
        for (final Face face : Face.VALUES) {
            if (isRenderingBackFace(face, dx, dy, dz)) {
                continue;
            }

            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

            setupMatrix(face);

            ensureSanity();

            if (!isPlayerHoldingKey() || !drawConfigOverlay(casing, face)) {
                drawModuleOverlay(casing, face, partialTicks);
            }

            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }

        RenderHelper.enableStandardItemLighting();

        GL11.glPopMatrix();
    }

    private boolean isRenderingBackFace(final Face face, final double dx, final double dy, final double dz) {
        final EnumFacing facing = Face.toEnumFacing(face.getOpposite());
        final double dotProduct = facing.getFrontOffsetX() * dx + facing.getFrontOffsetY() * (dy - Minecraft.getMinecraft().thePlayer.getEyeHeight()) + facing.getFrontOffsetZ() * dz;
        return dotProduct < 0;
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

    private void ensureSanity() {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        RenderUtil.bindTexture(TextureMap.locationBlocksTexture);
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderUtil.ignoreLighting();
    }

    private boolean drawConfigOverlay(final TileEntityCasing casing, final Face face) {
        // Only bother rendering the overlay if the player is nearby.
        if (!isPlayerKindaClose(casing)) {
            return false;
        }

        if (isPlayerSneaking() && !casing.isLocked()) {
            final TextureAtlasSprite closedSprite;
            final TextureAtlasSprite openSprite;

            final Port lookingAtPort;
            final boolean isLookingAt = isPlayerLookingAt(casing.getPositionX(), casing.getPositionY(), casing.getPositionZ(), face);
            if (isLookingAt) {
                closedSprite = RenderUtil.getSprite(TextureLoader.LOCATION_CASING_PORT_CLOSED_OVERLAY);
                openSprite = RenderUtil.getSprite(TextureLoader.LOCATION_CASING_PORT_OPEN_OVERLAY);

                final MovingObjectPosition hit = Minecraft.getMinecraft().objectMouseOver;
                final Vec3 uv = TransformUtil.hitToUV(face, Vec3.createVectorHelper(hit.blockX, hit.blockY, hit.blockZ).subtract(hit.hitVec));
                lookingAtPort = Port.fromUVQuadrant(uv);
            } else {
                closedSprite = RenderUtil.getSprite(TextureLoader.LOCATION_CASING_PORT_CLOSED_SMALL_OVERLAY);
                openSprite = null;

                lookingAtPort = null;
            }

            GL11.glPushMatrix();
            for (final Port port : Port.CLOCKWISE) {
                final boolean isClosed = casing.isReceivingPipeLocked(face, port);
                final TextureAtlasSprite sprite = isClosed ? closedSprite : openSprite;
                if (sprite != null) {
                    RenderUtil.drawQuad(sprite);
                }

                if (port == lookingAtPort) {
                    RenderUtil.drawQuad(RenderUtil.getSprite(TextureLoader.LOCATION_CASING_PORT_HIGHLIGHT_OVERLAY));
                }

                GL11.glTranslatef(0.5f, 0.5f, 0.5f);
                GL11.glRotatef(90, 0, 0, 1);
                GL11.glTranslatef(-0.5f, -0.5f, -0.5f);
            }
            GL11.glPopMatrix();

            return isLookingAt;
        } else {
            final TextureAtlasSprite sprite;
            if (casing.isLocked()) {
                sprite = RenderUtil.getSprite(TextureLoader.LOCATION_CASING_LOCKED_OVERLAY);
            } else {
                sprite = RenderUtil.getSprite(TextureLoader.LOCATION_CASING_UNLOCKED_OVERLAY);
            }

            RenderUtil.drawQuad(sprite);
        }

        return true;
    }

    private void drawModuleOverlay(final TileEntityCasing casing, final Face face, final float partialTicks) {
        final TextureAtlasSprite closedSprite = RenderUtil.getSprite(TextureLoader.LOCATION_CASING_PORT_CLOSED_SMALL_OVERLAY);

        GL11.glPushMatrix();
        GL11.glTranslated(0, 0, -0.005);
        for (final Port port : Port.CLOCKWISE) {
            final boolean isClosed = casing.isReceivingPipeLocked(face, port);
            if (isClosed) {
                RenderUtil.drawQuad(closedSprite);
            }

            GL11.glTranslatef(0.5f, 0.5f, 0.5f);
            GL11.glRotatef(90, 0, 0, 1);
            GL11.glTranslatef(-0.5f, -0.5f, -0.5f);
        }
        GL11.glPopMatrix();

        final Module module = casing.getModule(face);
        if (module == null) {
            return;
        }
        if (BLACKLIST.contains(module.getClass())) {
            return;
        }

        final EnumFacing facing = Face.toEnumFacing(face);
        final int neighborX = casing.getPositionX() + facing.getFrontOffsetX();
        final int neighborY = casing.getPositionY() + facing.getFrontOffsetY();
        final int neighborZ = casing.getPositionZ() + facing.getFrontOffsetZ();
        final int brightness = casing.getWorldObj().getLightBrightnessForSkyBlocks(neighborX, neighborY, neighborZ, 0);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, brightness % 65536, brightness / 65536);

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
        final EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        return Items.isKey(player.getHeldItem());
    }

    private boolean isPlayerSneaking() {
        final EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        return player.isSneaking();
    }

    private boolean isPlayerLookingAt(final int x, final int y, final int z, final Face face) {
        final MovingObjectPosition hit = Minecraft.getMinecraft().objectMouseOver;
        return hit != null && Face.fromIntFacing(hit.sideHit) == face &&
               hit.blockX == x && hit.blockY == y && hit.blockZ == z;
    }
}
