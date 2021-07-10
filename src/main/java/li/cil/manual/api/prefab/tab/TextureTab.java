package li.cil.manual.api.prefab.tab;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

/**
 * Simple implementation of a tab icon renderer using a full texture as its graphic.
 */
@OnlyIn(Dist.CLIENT)
public final class TextureTab extends AbstractTab {
    private final ResourceLocation location;

    public TextureTab(final String path, @Nullable final ITextComponent tooltip, final ResourceLocation location) {
        super(path, tooltip);
        this.location = location;
    }

    @Override
    public void renderIcon(final MatrixStack matrixStack) {
        Minecraft.getInstance().getTextureManager().bind(location);
        Screen.blit(matrixStack, 0, 0, 16, 16, 0, 0, 1, 1, 1, 1);
    }
}
