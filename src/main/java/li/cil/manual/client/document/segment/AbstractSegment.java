package li.cil.manual.client.document.segment;

import li.cil.manual.api.ManualModel;
import li.cil.manual.api.ManualStyle;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Pattern;

@OnlyIn(Dist.CLIENT)
abstract class AbstractSegment implements Segment {
    protected final ManualModel manual;
    protected final ManualStyle style;
    @Nullable private final Segment parent;
    protected Segment next;

    // --------------------------------------------------------------------- //

    protected AbstractSegment(final ManualModel manual, final ManualStyle style, @Nullable final Segment parent) {
        this.manual = manual;
        this.style = style;
        this.parent = parent;
    }

    // --------------------------------------------------------------------- //

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
