package li.cil.manual.client.document.segment;

import li.cil.manual.api.Manual;
import li.cil.manual.api.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Pattern;

@OnlyIn(Dist.CLIENT)
abstract class AbstractSegment implements Segment {
    protected final Manual manual;
    protected final Style style;
    @Nullable protected final Segment parent;
    protected Segment next;

    protected AbstractSegment(final Manual manual, final Style style, @Nullable final Segment parent) {
        this.manual = manual;
        this.style = style;
        this.parent = parent;
    }

    @Override
    public Segment getLineRoot() {
        return parent != null ? parent.getLineRoot() : this;
    }

    @Override
    public Optional<Segment> getParent() {
        return Optional.ofNullable(parent);
    }

    @Override
    public Iterable<Segment> refine(final Pattern pattern, final SegmentRefiner refiner) {
        return Collections.singletonList(this);
    }

    @Override
    public void setNext(@Nullable final Segment segment) {
        next = segment;
    }
}
