package li.cil.manual.client.document.segment.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import li.cil.manual.api.render.ContentRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.IOException;

@OnlyIn(Dist.CLIENT)
public class TextureContentRenderer implements ContentRenderer {
    private final ResourceLocation location;
    private final ImageTexture texture;

    // --------------------------------------------------------------------- //

    public TextureContentRenderer(final ResourceLocation location) {
        this.location = location;

        final TextureManager manager = Minecraft.getInstance().getTextureManager();
        final Texture image = manager.getTexture(location);
        if (image instanceof ImageTexture) {
            this.texture = (ImageTexture) image;
        } else {
            this.texture = new ImageTexture(location);
            manager.register(location, texture);
        }
    }

    // --------------------------------------------------------------------- //

    @Override
    public int getWidth() {
        return texture.width;
    }

    @Override
    public int getHeight() {
        return texture.height;
    }

    @Override
    public void render(final MatrixStack matrixStack, final int mouseX, final int mouseY) {
        Minecraft.getInstance().getTextureManager().bind(location);
        Screen.blit(matrixStack, 0, 0, getWidth(), getHeight(), 0, 0, 1, 1, 1, 1);
    }

    // --------------------------------------------------------------------- //

    private static class ImageTexture extends SimpleTexture {
        private int width = 0;
        private int height = 0;

        ImageTexture(final ResourceLocation location) {
            super(location);
        }

        @Override
        public void load(final IResourceManager manager) throws IOException {
            super.load(manager);
            try (final TextureData textureData = getTextureImage(manager)) {
                final NativeImage nativeImage = textureData.getImage();
                width = nativeImage.getWidth();
                height = nativeImage.getHeight();
            }
        }
    }
}
