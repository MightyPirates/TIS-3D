package li.cil.tis3d.client.manual.segment.render;

import li.cil.tis3d.api.manual.ImageRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class TextureImageRenderer implements ImageRenderer {
    private final ResourceLocation location;
    private final ImageTexture texture;

    public TextureImageRenderer(final ResourceLocation location) {
        this.location = location;

        final TextureManager manager = Minecraft.getMinecraft().getTextureManager();
        final ITextureObject image = manager.getTexture(location);
        if (image instanceof ImageTexture) {
            this.texture = (ImageTexture) image;
        } else {
            if (image != null && image.getGlTextureId() != -1) {
                TextureUtil.deleteTexture(image.getGlTextureId());
            }
            this.texture = new ImageTexture(location);
            manager.loadTexture(location, texture);
        }
    }

    @Override
    public int getWidth() {
        return texture.width;
    }

    @Override
    public int getHeight() {
        return texture.height;
    }

    @Override
    public void render(final int mouseX, final int mouseY) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(location);
        GlStateManager.color(1, 1, 1, 1);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0, 0);
        GL11.glVertex2f(0, 0);
        GL11.glTexCoord2f(0, 1);
        GL11.glVertex2f(0, texture.height);
        GL11.glTexCoord2f(1, 1);
        GL11.glVertex2f(texture.width, texture.height);
        GL11.glTexCoord2f(1, 0);
        GL11.glVertex2f(texture.width, 0);
        GL11.glEnd();
    }

    private static class ImageTexture extends AbstractTexture {
        private final ResourceLocation location;
        private int width = 0;
        private int height = 0;

        ImageTexture(final ResourceLocation location) {
            this.location = location;
        }

        @Override
        public void loadTexture(final IResourceManager manager) throws IOException {
            deleteGlTexture();

            final IResource resource = manager.getResource(location);
            try (InputStream is = resource.getInputStream()) {
                final BufferedImage bi = ImageIO.read(is);
                TextureUtil.uploadTextureImageAllocate(getGlTextureId(), bi, false, false);
                width = bi.getWidth();
                height = bi.getHeight();
            }
        }
    }
}
