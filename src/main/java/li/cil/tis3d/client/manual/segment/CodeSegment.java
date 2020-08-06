package li.cil.tis3d.client.manual.segment;

import com.mojang.blaze3d.platform.GlStateManager;
import li.cil.tis3d.client.render.font.NormalFontRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public final class CodeSegment extends BasicTextSegment {
    private static final float FONT_SCALE = 0.5f;
    private static final int OFFSET_Y = 1;

    private final Segment parent;
    private final String text;

    public CodeSegment(final Segment parent, final String text) {
        this.parent = parent;
        this.text = text;
    }

    @Override
    public Segment parent() {
        return parent;
    }

    @Override
    protected String text() {
        return text;
    }

    @Override
    public Optional<InteractiveSegment> render(final MatrixStack matrices, final int x, final int y, final int indent, final int maxWidth, final TextRenderer renderer, final int mouseX, final int mouseY) {
        int currentX = x + indent;
        int currentY = y + OFFSET_Y;
        String chars = text();
        final int wrapIndent = computeWrapIndent(renderer);
        int numChars = maxChars(chars, maxWidth - indent, maxWidth - wrapIndent, renderer);
        while (chars.length() > 0) {
            final String part = chars.substring(0, numChars);
            GlStateManager.color4f(0.25f, 0.3f, 0.5f, 1);
            GlStateManager.pushMatrix();
            GlStateManager.translatef(currentX, currentY, 0);
            GlStateManager.scalef(FONT_SCALE, FONT_SCALE, FONT_SCALE);
            NormalFontRenderer.INSTANCE.drawString(part.toUpperCase());
            GlStateManager.popMatrix();
            currentX = x + wrapIndent;
            currentY += lineHeight(renderer);
            chars = chars.substring(numChars);
            chars = chars.substring(indexOfFirstNonWhitespace(chars));
            numChars = maxChars(chars, maxWidth - wrapIndent, maxWidth - wrapIndent, renderer);
        }

        return Optional.empty();
    }

    @Override
    protected boolean ignoreLeadingWhitespace() {
        return false;
    }

    @Override
    protected int stringWidth(final String s, final TextRenderer renderer) {
        return (int)(FONT_SCALE * s.length() * (NormalFontRenderer.INSTANCE.getCharWidth() + 1));
    }

    @Override
    public String toString() {
        return String.format("`%s`", text());
    }
}
