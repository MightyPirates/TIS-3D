package li.cil.manual.client.document.segment;


import li.cil.manual.api.ManualModel;
import li.cil.manual.api.ManualStyle;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class BoldSegment extends TextSegment {
    public BoldSegment(final ManualModel manual, final ManualStyle style, final Segment parent, final String text) {
        super(manual, style, parent, text);
    }

    // --------------------------------------------------------------------- //

    @Override
    public String toString() {
        return String.format("**%s**", super.toString());
    }

    // --------------------------------------------------------------------- //

    @Override
    protected String getFormat() {
        return super.getFormat() + TextFormatting.BOLD;
    }
}
