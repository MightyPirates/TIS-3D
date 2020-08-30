package li.cil.tis3d.client.api;

import li.cil.tis3d.api.ClientAPI;
import li.cil.tis3d.api.detail.FontRendererAPI;
import li.cil.tis3d.client.render.font.SmallFontRenderer;
import li.cil.tis3d.client.render.font.NormalFontRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Provide access to our tiny mono-space font.
 */
@Environment(EnvType.CLIENT)
public final class FontRendererAPIImpl implements FontRendererAPI {
    @Override
    public void drawString(final String string) {
        SmallFontRenderer.INSTANCE.drawString(string);
    }

    @Override
    public void drawString(final String string, final int maxChars) {
        SmallFontRenderer.INSTANCE.drawString(string, maxChars);
    }

    @Override
    public void drawString(final Font font, final MatrixStack.Entry matrices, final VertexConsumer vc,
                           final int light, final int overlay, final int color,
                           final CharSequence value, final int maxChars) {
        switch (font) {
        case SmallFont:
            SmallFontRenderer.INSTANCE.drawString(matrices, vc, light, overlay, color, value, maxChars);
            break;

        case NormalFont:
            NormalFontRenderer.INSTANCE.drawString(matrices, vc, light, overlay, color, value, maxChars);
            break;

        default:
            throw new RuntimeException("Font " + font + " not implemented");
        }
    }

    @Override
    public VertexConsumer chooseVertexConsumer(final Font font, final VertexConsumerProvider vcp) {
        switch (font) {
        case SmallFont:
            return SmallFontRenderer.INSTANCE.chooseVertexConsumer(vcp);

        case NormalFont:
            return NormalFontRenderer.INSTANCE.chooseVertexConsumer(vcp);

        default:
            throw new RuntimeException("Font " + font + " not implemented");
        }
    }

    @Override
    public int getCharWidth() {
        return SmallFontRenderer.INSTANCE.getCharWidth();
    }

    @Override
    public int getCharHeight() {
        return SmallFontRenderer.INSTANCE.getCharHeight();
    }
}
