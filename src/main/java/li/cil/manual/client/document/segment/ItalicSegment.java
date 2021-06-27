package li.cil.manual.client.document.segment;

import li.cil.manual.api.Manual;
import li.cil.manual.api.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ItalicSegment extends TextSegment {
    public ItalicSegment(final Manual manual, final Style style, final Segment parent, final String text) {
        super(manual, style, parent, text);
    }

    @Override
    protected String getFormat() {
        return super.getFormat() + TextFormatting.ITALIC;
    }

    @Override
    public String toString() {
        return String.format("*%s*", super.toString());
    }
}
