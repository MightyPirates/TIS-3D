package li.cil.tis3d.api.prefab.manual;

import li.cil.tis3d.api.manual.TabIconRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
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

    public void render() {
        Minecraft.getMinecraft().getTextureManager().bindTexture(location);
        final Tessellator t = Tessellator.getInstance();
        final BufferBuilder b = t.getBuffer();
        b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        b.pos(0, 16, 0).tex(0, 1).endVertex();
        b.pos(16, 16, 0).tex(1, 1).endVertex();
        b.pos(16, 0, 0).tex(1, 0).endVertex();
        b.pos(0, 0, 0).tex(0, 0).endVertex();
        t.draw();
    }
}
