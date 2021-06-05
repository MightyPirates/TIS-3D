package li.cil.tis3d.api.prefab.manual;

import com.mojang.blaze3d.systems.RenderSystem;
import li.cil.tis3d.api.manual.TabIconRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

/**
 * Simple implementation of a tab icon renderer using a full texture as its graphic.
 */
@SuppressWarnings("UnusedDeclaration")
@Environment(EnvType.CLIENT)
public class TextureTabIconRenderer implements TabIconRenderer {
    private final Identifier location;

    public TextureTabIconRenderer(final Identifier location) {
        this.location = location;
    }

    @Override
    public void render() {
        MinecraftClient.getInstance().getTextureManager().bindTexture(location);
        RenderSystem.enableBlend();
        final Tessellator t = Tessellator.getInstance();
        final BufferBuilder b = t.getBuffer();
        b.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        b.vertex(0, 16, 0).texture(0, 1).next();
        b.vertex(16, 16, 0).texture(1, 1).next();
        b.vertex(16, 0, 0).texture(1, 0).next();
        b.vertex(0, 0, 0).texture(0, 0).next();
        t.draw();
        RenderSystem.disableBlend();
    }
}
