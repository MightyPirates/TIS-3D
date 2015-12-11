package li.cil.tis3d.client.manual.segment;

import li.cil.tis3d.api.FontRendererAPI;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.util.Optional;

public final class CodeSegment extends BasicTextSegment {
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
    public Optional<InteractiveSegment> render(final int x, final int y, final int indent, final int maxWidth, final FontRenderer renderer, final int mouseX, final int mouseY) {
        int currentX = x + indent;
        int currentY = y;
        String chars = text();
        final int wrapIndent = computeWrapIndent(renderer);
        int numChars = maxChars(chars, maxWidth - indent, maxWidth - wrapIndent, renderer);
        while (chars.length() > 0) {
            final String part = chars.substring(0, numChars);
            GL11.glColor4f(0.25f, 0.3f, 0.5f, 1);
            GlStateManager.pushMatrix();
            GlStateManager.translate(currentX, currentY, 0);
            GlStateManager.scale(1.75f, 1.75f, 1.75f);
            FontRendererAPI.drawString(part.toUpperCase());
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
    protected int stringWidth(final String s, final FontRenderer renderer) {
        return (int)(1.75f * s.length() * (FontRendererAPI.getCharWidth() + 1));
    }

    @Override
    public String toString() {
        return String.format("`%s`", text());
    }
}
