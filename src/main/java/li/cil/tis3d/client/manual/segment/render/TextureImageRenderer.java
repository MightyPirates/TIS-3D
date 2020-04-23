package li.cil.tis3d.client.manual.segment.render;

import com.mojang.blaze3d.platform.GlStateManager;
import li.cil.tis3d.api.manual.ImageRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.texture.TextureUtil;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.io.InputStream;

public class TextureImageRenderer implements ImageRenderer {
    private final Identifier location;
    private final ImageTexture texture;

    public TextureImageRenderer(final Identifier location) {
        this.location = location;

        final TextureManager manager = MinecraftClient.getInstance().getTextureManager();
        final AbstractTexture image = manager.getTexture(location);
        if (image instanceof ImageTexture) {
            this.texture = (ImageTexture)image;
        } else {
            if (image != null && image.getGlId() != -1) {
                TextureUtil.releaseTextureId(image.getGlId());
            }
            this.texture = new ImageTexture(location);
            manager.registerTexture(location, texture);
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
        MinecraftClient.getInstance().getTextureManager().bindTexture(location);
        GlStateManager.color4f(1, 1, 1, 1);
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
        private final Identifier location;
        private int width = 0;
        private int height = 0;

        ImageTexture(final Identifier location) {
            this.location = location;
        }

        @Override
        public void load(final ResourceManager manager) throws IOException {
            this.clearGlId();

            final Resource resource = manager.getResource(location);
            try (final InputStream is = resource.getInputStream()) {
                final NativeImage bi = NativeImage.read(is);

                TextureUtil.prepareImage(this.getGlId(), bi.getWidth(), bi.getHeight());
                bi.upload(0, 0, 0, false);

                width = bi.getWidth();
                height = bi.getHeight();
            }
        }
    }
}
