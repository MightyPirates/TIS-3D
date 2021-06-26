package li.cil.manual.client.document.segment;

import com.mojang.blaze3d.matrix.MatrixStack;
import li.cil.manual.api.Manual;
import li.cil.tis3d.client.renderer.font.NormalFontRenderer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public final class MonospaceSegment extends BasicTextSegment {
    private static final float FONT_SCALE = 0.5f;
    private static final int OFFSET_Y = 1;
    private static final int CODE_TEXT_COLOR = 0xFF404D80;

    private final Segment parent;
    private final String text;

    public MonospaceSegment(final Manual manual, final Segment parent, final String text) {
        super(manual);
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
    public Optional<InteractiveSegment> render(final MatrixStack matrixStack, final int x, final int y, final int indent, final int maxWidth, final FontRenderer renderer, final int mouseX, final int mouseY) {
        final IRenderTypeBuffer.Impl bufferFactory = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());

        int currentX = x + indent;
        int currentY = y + OFFSET_Y;
        String chars = text();
        final int wrapIndent = computeWrapIndent(renderer);
        int numChars = maxChars(chars, maxWidth - indent, maxWidth - wrapIndent, renderer);
        while (chars.length() > 0) {
            final String part = chars.substring(0, numChars);
            matrixStack.pushPose();
            matrixStack.translate(currentX, currentY, 0);
            matrixStack.scale(FONT_SCALE, FONT_SCALE, FONT_SCALE);

            NormalFontRenderer.INSTANCE.drawString(matrixStack, bufferFactory, part.toUpperCase(), CODE_TEXT_COLOR);
            matrixStack.popPose();

            currentX = x + wrapIndent;
            currentY += lineHeight(renderer);
            chars = chars.substring(numChars);
            chars = chars.substring(indexOfFirstNonWhitespace(chars));
            numChars = maxChars(chars, maxWidth - wrapIndent, maxWidth - wrapIndent, renderer);
        }

        bufferFactory.endBatch();

        return Optional.empty();
    }

    @Override
    protected boolean ignoreLeadingWhitespace() {
        return false;
    }

    @Override
    protected int stringWidth(final String s, final FontRenderer renderer) {
        return (int) (FONT_SCALE * s.length() * (NormalFontRenderer.INSTANCE.getCharWidth() + 1));
    }

    @Override
    public String toString() {
        return String.format("`%s`", text());
    }
}
