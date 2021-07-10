package li.cil.manual.client.document.segment;

import li.cil.manual.api.ManualModel;
import li.cil.manual.api.ManualStyle;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public final class HeaderSegment extends TextSegment {
    private final int level;
    private final float fontScale;

    // --------------------------------------------------------------------- //

    public HeaderSegment(final ManualModel manual, final ManualStyle style, final Segment parent, final String text, final int level) {
        super(manual, style, parent, text);
        this.level = level;
        fontScale = Math.max(1, 1.75f - level * 0.25f);
    }

    // --------------------------------------------------------------------- //

    @Override
    public String toString() {
        return String.format("%s %s", StringUtils.repeat('#', level), super.toString());
    }

    // --------------------------------------------------------------------- //

    @Override
    protected float getScale() {
        return fontScale * super.getScale();
    }

    @Override
    protected String getFormat() {
        return super.getFormat() + TextFormatting.UNDERLINE;
    }

    @Override
    protected int getLineHeight() {
        return (int) (1.4f * super.getLineHeight());
    }
}
