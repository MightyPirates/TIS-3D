package li.cil.manual.client.document.segment;

import com.mojang.blaze3d.matrix.MatrixStack;
import li.cil.manual.api.Manual;
import li.cil.manual.api.Style;
import li.cil.manual.api.render.ContentRenderer;
import li.cil.manual.api.render.InteractiveContentRenderer;
import li.cil.tis3d.util.Color;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public final class RenderSegment extends AbstractSegment implements InteractiveSegment {
    private final ITextComponent title;
    private final ContentRenderer renderer;

    // --------------------------------------------------------------------- //

    public RenderSegment(final Manual manual, final Style style, final Segment parent, final ITextComponent title, final ContentRenderer renderer) {
        super(manual, style, parent);
        this.title = title;
        this.renderer = renderer;
    }

    // --------------------------------------------------------------------- //

    @Override
    public Optional<ITextComponent> getTooltip() {
        if (renderer instanceof InteractiveContentRenderer) {
            return Optional.of(((InteractiveContentRenderer) renderer).getTooltip(title));
        } else {
            return Optional.of(title);
        }
    }

    @Override
    public boolean mouseClicked() {
        return renderer instanceof InteractiveContentRenderer && ((InteractiveContentRenderer) renderer).
            mouseClicked();
    }

    @Override
    public int getLineHeight(final int segmentX, final int documentWidth) {
        return imageHeight(segmentX, documentWidth);
    }

    @Override
    public Optional<InteractiveSegment> render(final MatrixStack matrixStack, final int segmentX, final int lineHeight, final int documentWidth, final int mouseX, final int mouseY) {
        final int width = imageWidth(segmentX, documentWidth);
        final int height = imageHeight(segmentX, documentWidth);

        final boolean wrapBefore = segmentX >= documentWidth;
        final boolean centerAndWrapAfter = segmentX == 0 || wrapBefore;

        final int x = centerAndWrapAfter ? (documentWidth - width) / 2 : segmentX;
        final int y = wrapBefore ? lineHeight : 0;

        final float scale = scale(segmentX, documentWidth);

        matrixStack.pushPose();
        matrixStack.translate(x, y, 0);
        matrixStack.scale(scale, scale, scale);

        final boolean isHovered = mouseX >= x && mouseX <= x + width &&
                                  mouseY >= y && mouseY <= y + height;
        if (isHovered) {
            Screen.fill(matrixStack, 0, 0, renderer.getWidth(), renderer.getHeight(), Color.withAlpha(Color.CYAN, 0.25f));
        }

        renderer.render(matrixStack, mouseX, mouseY);

        matrixStack.popPose();

        return isHovered ? Optional.of(this) : Optional.empty();
    }

    @Override
    public NextSegmentInfo getNext(final int segmentX, final int lineHeight, final int documentWidth) {
        final int width = imageWidth(segmentX, documentWidth);
        final int height = imageHeight(segmentX, documentWidth);

        final boolean wrapBefore = segmentX >= documentWidth;
        final boolean centerAndWrapAfter = segmentX == 0 || wrapBefore;

        final int localX = centerAndWrapAfter ? (documentWidth - width) / 2 : segmentX;
        final int localY = wrapBefore ? lineHeight : 0;

        final int absoluteX = centerAndWrapAfter ? 0 : (localX + width);
        final int relativeY = localY + (centerAndWrapAfter ? height + 1 : 0);
        return new NextSegmentInfo(next, absoluteX, relativeY);
    }

    @Override
    public String toString() {
        return String.format("![%s](%s)", title, renderer);
    }

    // --------------------------------------------------------------------- //

    private int imageWidth(final int segmentX, final int documentWidth) {
        if (segmentX >= documentWidth) {
            return Math.min(documentWidth, renderer.getWidth());
        } else {
            return Math.min(documentWidth - segmentX, renderer.getWidth());
        }
    }

    private int imageHeight(final int segmentX, final int documentWidth) {
        return MathHelper.ceil(renderer.getHeight() * scale(segmentX, documentWidth));
    }

    private float scale(final int segmentX, final int documentWidth) {
        // Only scale down; if necessary, scale down to fill remainder of current line.
        return Math.min(1, imageWidth(segmentX, documentWidth) / (float) renderer.getWidth());
    }
}
