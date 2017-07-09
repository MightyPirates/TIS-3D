package li.cil.tis3d.client.renderer.tileentity;

import li.cil.tis3d.common.tileentity.TileEntityController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.opengl.GL11;

public class TileEntitySpecialRendererController extends TileEntitySpecialRenderer {
    @Override
    public void renderTileEntityAt(final TileEntity tileEntity, final double x, final double y, final double z, final float partialTicks) {
        final TileEntityController controller = (TileEntityController) tileEntity;
        final TileEntityController.ControllerState state = controller.getState();
        if (!state.isError) {
            return;
        }

        final MovingObjectPosition hit = Minecraft.getMinecraft().objectMouseOver;
        if (hit != null && hit.blockX == controller.xCoord && hit.blockY == controller.yCoord && hit.blockZ == controller.zCoord) {
            setLightmapDisabled(true);
            drawNameplate(controller, I18n.format(state.translateKey), x, y, z, 12);
            setLightmapDisabled(false);
        }
    }

    private static void setLightmapDisabled(final boolean disabled) {
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);

        if (disabled) {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
        } else {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }

        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    // Future TileEntitySpecialRenderer.drawNameplate
    private void drawNameplate(final TileEntity te, final String str, final double x, final double y, final double z, final int maxDistance) {
        final Entity entity = TileEntityRendererDispatcher.instance.field_147551_g;
        final double sqrDistance = te.getDistanceFrom(entity.posX, entity.posY, entity.posZ);

        if (sqrDistance <= maxDistance * maxDistance) {
            final float yaw = TileEntityRendererDispatcher.instance.field_147562_h;
            final float pitch = TileEntityRendererDispatcher.instance.field_147563_i;
            drawNameplate(str, (float) x + 0.5f, (float) y + 1.5f, (float) z + 0.5f, yaw, pitch);
        }
    }

    // Render.func_147906_a
    private static void drawNameplate(final String str, final double x, final double y, final double z, final float yaw, final float pitch) {
        final FontRenderer fontrenderer = TileEntityRendererDispatcher.instance.getFontRenderer();
        final float f = 1.6F;

        final float f1 = 0.016666668F * f;
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x, (float) y, (float) z);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-yaw, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(pitch, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-f1, -f1, f1);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        final Tessellator tessellator = Tessellator.instance;
        byte b0 = 0;

        if (str.equals("deadmau5")) {
            b0 = -10;
        }

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        tessellator.startDrawingQuads();
        final int j = fontrenderer.getStringWidth(str) / 2;
        tessellator.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
        tessellator.addVertex((double) (-j - 1), (double) (-1 + b0), 0.0D);
        tessellator.addVertex((double) (-j - 1), (double) (8 + b0), 0.0D);
        tessellator.addVertex((double) (j + 1), (double) (8 + b0), 0.0D);
        tessellator.addVertex((double) (j + 1), (double) (-1 + b0), 0.0D);
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, b0, 553648127);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, b0, -1);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
    }
}