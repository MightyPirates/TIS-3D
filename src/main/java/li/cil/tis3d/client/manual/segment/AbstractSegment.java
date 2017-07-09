package li.cil.tis3d.client.manual.segment;

import net.minecraft.client.gui.FontRenderer;

import java.util.Collections;
import java.util.Optional;
import java.util.regex.Pattern;

abstract class AbstractSegment implements Segment {
    private Segment next;

    @Override
    public Segment root() {
        final Segment parent = parent();
        return parent == null ? this : parent.root();
    }

    @Override
    public Optional<InteractiveSegment> render(final int x, final int y, final int indent, final int maxWidth, final FontRenderer renderer, final int mouseX, final int mouseY) {
        return Optional.empty();
    }

    @Override
    public Iterable<Segment> refine(final Pattern pattern, final SegmentRefiner refiner) {
        return Collections.singletonList(this);
    }

    @Override
    public Segment next() {
        return next;
    }

    @Override
    public void setNext(final Segment segment) {
        next = segment;
    }
}
