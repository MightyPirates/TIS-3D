package li.cil.manual.client.document.segment;

import li.cil.manual.api.Manual;
import li.cil.manual.api.render.FontRenderer;
import li.cil.manual.api.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class MonospaceSegment extends TextSegment {
    public MonospaceSegment(final Manual manual, final Style style, final Segment parent, final String text) {
        super(manual, style, parent, text);
    }

    @Override
    protected boolean isIgnoringLeadingWhitespace() {
        return false;
    }

    @Override
    protected int getColor() {
        return style.getMonospaceTextColor();
    }

    @Override
    protected FontRenderer getFont() {
        return style.getMonospaceFont();
    }

    @Override
    public String toString() {
        return String.format("`%s`", super.toString());
    }
}
