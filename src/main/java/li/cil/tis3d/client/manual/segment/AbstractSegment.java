package li.cil.tis3d.client.manual.segment;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Pattern;

@OnlyIn(Dist.CLIENT)
abstract class AbstractSegment implements Segment {
    private Segment next;

    @Override
    public Segment root() {
        final Segment parent = parent();
        return parent == null ? this : parent.root();
    }

    @Override
    public Optional<InteractiveSegment> render(final MatrixStack matrixStack, final int x, final int y, final int indent, final int maxWidth, final FontRenderer renderer, final int mouseX, final int mouseY) {
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
    public void setNext(@Nullable final Segment segment) {
        next = segment;
    }
}
