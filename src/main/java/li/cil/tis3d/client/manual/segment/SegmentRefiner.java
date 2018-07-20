package li.cil.tis3d.client.manual.segment;

import java.util.regex.Matcher;

@FunctionalInterface
public interface SegmentRefiner {
    Segment refine(final Segment segment, final Matcher matcher);
}
