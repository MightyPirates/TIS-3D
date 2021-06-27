package li.cil.manual.client.document.segment;

import li.cil.manual.api.Manual;
import li.cil.manual.api.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.regex.Matcher;

@FunctionalInterface
@OnlyIn(Dist.CLIENT)
public interface SegmentRefiner {
    Segment refine(final Manual manual, final Style style, final Segment segment, final Matcher matcher);
}
