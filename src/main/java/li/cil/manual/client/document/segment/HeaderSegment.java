package li.cil.manual.client.document.segment;

import joptsimple.internal.Strings;
import li.cil.manual.api.Manual;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public final class HeaderSegment extends TextSegment {
    private final int level;
    private final float fontScale;

    public HeaderSegment(final Manual manual, final Segment parent, final String text, final int level) {
        super(manual, parent, text);
        this.level = level;
        fontScale = Math.max(2, 5 - level) / 2f;
    }

    @Override
    protected Optional<Float> scale() {
        return Optional.of(fontScale);
    }

    @Override
    protected String format() {
        return TextFormatting.UNDERLINE.toString();
    }

    @Override
    public String toString() {
        return String.format("%s %s", Strings.repeat('#', level), text());
    }
}
