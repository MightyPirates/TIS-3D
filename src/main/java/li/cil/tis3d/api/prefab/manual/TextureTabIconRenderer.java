package li.cil.tis3d.api.prefab.manual;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;

/**
 * Simple implementation of a tab icon renderer using a full texture as its graphic.
 */
public class TextureTabIconRenderer extends AbstractTab {
    private final ResourceLocation location;

    public TextureTabIconRenderer(final String path, @Nullable final ITextComponent tooltip, final ResourceLocation location) {
        super(path, tooltip);
        this.location = location;
    }

    @Override
    public void renderIcon(final MatrixStack matrixStack) {
        Minecraft.getInstance().getTextureManager().bindTexture(location);
        Screen.blit(matrixStack, 0, 0, 16, 16, 0, 0, 1, 1, 1, 1);
    }
}
