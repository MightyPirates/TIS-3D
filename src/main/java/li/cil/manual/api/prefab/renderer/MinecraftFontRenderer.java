package li.cil.manual.api.prefab.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import li.cil.manual.api.render.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.text.ITextComponent;

public final class MinecraftFontRenderer implements FontRenderer {
    private final net.minecraft.client.gui.FontRenderer font;

    public MinecraftFontRenderer(final net.minecraft.client.gui.FontRenderer font) {
        this.font = font;
    }

    @Override
    public void draw(final MatrixStack matrixStack, final CharSequence value, final int argb) {
        font.draw(matrixStack, value.toString(), 0, 0, argb);
    }

    @Override
    public void drawBatch(final MatrixStack matrixStack, final IRenderTypeBuffer buffer, final CharSequence value, final int argb) {
        font.drawInBatch(value.toString(), 0, 0, argb, false, matrixStack.last().pose(), buffer, false, 0, LightTexture.pack(0xF, 0xF));
    }

    @Override
    public int width(final CharSequence value) {
        return font.width(value.toString());
    }

    @Override
    public int width(final ITextComponent value) {
        return font.width(value);
    }

    @Override
    public int lineHeight() {
        return font.lineHeight;
    }
}
