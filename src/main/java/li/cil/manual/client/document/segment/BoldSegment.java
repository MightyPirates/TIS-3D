package li.cil.manual.client.document.segment;


import li.cil.manual.api.Manual;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class BoldSegment extends TextSegment {
    public BoldSegment(final Manual manual, final Segment parent, final String text) {
        super(manual, parent, text);
    }

    @Override
    protected String format() {
        return TextFormatting.BOLD.toString();
    }

    @Override
    public String toString() {
        return String.format("**%s**", text());
    }
}
