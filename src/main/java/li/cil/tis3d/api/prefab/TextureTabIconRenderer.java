package li.cil.tis3d.api.prefab;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import li.cil.tis3d.api.manual.TabIconRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Simple implementation of a tab icon renderer using a full texture as its graphic.
 */
@SuppressWarnings("UnusedDeclaration")
public class TextureTabIconRenderer implements TabIconRenderer {
    private final ResourceLocation location;

    public TextureTabIconRenderer(final ResourceLocation location) {
        this.location = location;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render() {
        Minecraft.getMinecraft().getTextureManager().bindTexture(location);
        //GL11.glBindTexture(GL11.GL_TEXTURE_2D, Minecraft.getMinecraft().getTextureManager().getTexture(location).getGlTextureId());
        final Tessellator t = Tessellator.instance;
        t.startDrawing(GL11.GL_QUADS);
        t.addVertexWithUV(0, 16, 0, 0, 1);
        t.addVertexWithUV(16, 16, 0, 1, 1);
        t.addVertexWithUV(16, 0, 0, 1, 0);
        t.addVertexWithUV(0, 0, 0, 0, 0);
        t.draw();
    }
}