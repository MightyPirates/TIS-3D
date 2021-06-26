package li.cil.manual.client.document.segment;

import li.cil.manual.api.Manual;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.regex.Matcher;

@FunctionalInterface
@OnlyIn(Dist.CLIENT)
public interface SegmentRefiner {
    Segment refine(final Manual manual, final Segment segment, final Matcher matcher);
}
