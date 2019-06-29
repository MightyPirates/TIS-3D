package li.cil.tis3d.client.manual.segment;

import net.minecraft.util.Formatting;

public final class StrikethroughSegment extends TextSegment {
    public StrikethroughSegment(final Segment parent, final String text) {
        super(parent, text);
    }

    @Override
    protected String format() {
        return Formatting.STRIKETHROUGH.toString();
    }

    @Override
    public String toString() {
        return String.format("~~%s~~", text());
    }
}
