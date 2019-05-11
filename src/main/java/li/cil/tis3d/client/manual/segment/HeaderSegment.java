package li.cil.tis3d.client.manual.segment;

import joptsimple.internal.Strings;
import net.minecraft.ChatFormat;
import java.util.Optional;

public final class HeaderSegment extends TextSegment {
    private final int level;
    private final float fontScale;

    public HeaderSegment(final Segment parent, final String text, final int level) {
        super(parent, text);
        this.level = level;
        fontScale = Math.max(2, 5 - level) / 2f;
    }

    @Override
    protected Optional<Float> scale() {
        return Optional.of(fontScale);
    }

    @Override
    protected String format() {
        return ChatFormat.UNDERLINE.toString();
    }

    @Override
    public String toString() {
        return String.format("%s %s", Strings.repeat('#', level), text());
    }
}
