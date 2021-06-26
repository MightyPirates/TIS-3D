package li.cil.manual.client.document.segment;

import li.cil.manual.api.Manual;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class StrikethroughSegment extends TextSegment {
    public StrikethroughSegment(final Manual manual, final Segment parent, final String text) {
        super(manual, parent, text);
    }

    @Override
    protected String format() {
        return TextFormatting.STRIKETHROUGH.toString();
    }

    @Override
    public String toString() {
        return String.format("~~%s~~", text());
    }
}
