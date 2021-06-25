package li.cil.tis3d.client.manual.segment;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.regex.Matcher;

@FunctionalInterface
@OnlyIn(Dist.CLIENT)
public interface SegmentRefiner {
    Segment refine(final Segment segment, final Matcher matcher);
}
