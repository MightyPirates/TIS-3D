package li.cil.tis3d.api.prefab.manual;

import com.mojang.blaze3d.systems.RenderSystem;
import li.cil.tis3d.api.manual.TabIconRenderer;
import li.cil.tis3d.client.init.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.*;
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
    public void render(int x, int y) {
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, location);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        final Tessellator t = Tessellator.getInstance();
        final BufferBuilder b = t.getBuffer();
        b.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        b.vertex(x, y + 16, 0).texture(0, 1).next();
        b.vertex(x + 16, y + 16, 0).texture(1, 1).next();
        b.vertex(x +16, y, 0).texture(1, 0).next();
        b.vertex(x, y, 0).texture(0, 0).next();
        t.draw();
        RenderSystem.disableBlend();
    }
}
