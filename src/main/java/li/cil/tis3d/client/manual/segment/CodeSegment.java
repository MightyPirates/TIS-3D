package li.cil.tis3d.client.manual.segment;

import li.cil.tis3d.client.renderer.font.FontRendererNormal;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.opengl.GL11;

import java.util.Optional;

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
    public Optional<InteractiveSegment> render(final int x, final int y, final int indent, final int maxWidth, final FontRenderer renderer, final int mouseX, final int mouseY) {
        int currentX = x + indent;
        int currentY = y + OFFSET_Y;
        String chars = text();
        final int wrapIndent = computeWrapIndent(renderer);
        int numChars = maxChars(chars, maxWidth - indent, maxWidth - wrapIndent, renderer);
        while (chars.length() > 0) {
            final String part = chars.substring(0, numChars);
            GL11.glColor4f(0.25f, 0.3f, 0.5f, 1);
            GL11.glPushMatrix();
            GL11.glTranslatef(currentX, currentY, 0);
            GL11.glScalef(FONT_SCALE, FONT_SCALE, FONT_SCALE);
            FontRendererNormal.INSTANCE.drawString(part.toUpperCase());
            GL11.glPopMatrix();
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
        return (int) (FONT_SCALE * s.length() * (FontRendererNormal.INSTANCE.getCharWidth() + 1));
    }

    @Override
    public String toString() {
        return String.format("`%s`", text());
    }
}
