package li.cil.tis3d.client.manual.segment;

import com.mojang.blaze3d.matrix.MatrixStack;
import li.cil.tis3d.api.manual.ContentRenderer;
import li.cil.tis3d.api.manual.InteractiveContentRenderer;
import li.cil.tis3d.client.manual.Document;
import li.cil.tis3d.util.Color;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

import java.util.Optional;

public final class RenderSegment extends AbstractSegment implements InteractiveSegment {
    private final Segment parent;
    private final ITextComponent title;
    private final ContentRenderer contentRenderer;

    private int lastX = 0;
    private int lastY = 0;

    public RenderSegment(final Segment parent, final ITextComponent title, final ContentRenderer contentRenderer) {
        this.parent = parent;
        this.title = title;
        this.contentRenderer = contentRenderer;
    }

    @Override
    public Segment parent() {
        return this.parent;
    }

    @Override
    public Optional<ITextComponent> tooltip() {
        if (contentRenderer instanceof InteractiveContentRenderer) {
            return Optional.of(((InteractiveContentRenderer) contentRenderer).getTooltip(title));
        } else {
            return Optional.of(title);
        }
    }

    @Override
    public boolean onMouseClick(final double mouseX, final double mouseY) {
        return contentRenderer instanceof InteractiveContentRenderer &&
               ((InteractiveContentRenderer) contentRenderer).onMouseClick(mouseX - lastX, mouseY - lastY);
    }

    @Override
    public void notifyHover() {
    }

    @Override
    public int nextY(final int indent, final int maxWidth, final FontRenderer renderer) {
        return imageHeight(maxWidth) + ((indent > 0) ? Document.lineHeight(renderer) : 0);
    }

    @Override
    public int nextX(final int indent, final int maxWidth, final FontRenderer renderer) {
        return 0;
    }

    @Override
    public Optional<InteractiveSegment> render(final MatrixStack matrixStack, final int x, final int y, final int indent, final int maxWidth, final FontRenderer renderer, final int mouseX, final int mouseY) {
        final int width = imageWidth(maxWidth);
        final int height = imageHeight(maxWidth);
        final int xOffset = (maxWidth - width) / 2;
        final int yOffset = 2 + ((indent > 0) ? Document.lineHeight(renderer) : 0);
        final float s = scale(maxWidth);

        lastX = x + xOffset;
        lastY = y + yOffset;

        final Optional<InteractiveSegment> hovered = checkHovered(mouseX, mouseY, x + xOffset, y + yOffset, width, height);

        matrixStack.pushPose();
        matrixStack.translate(x + xOffset, y + yOffset, 0);
        matrixStack.scale(s, s, s);

        if (hovered.isPresent()) {
            Screen.fill(matrixStack, 0, 0, contentRenderer.getWidth(), contentRenderer.getHeight(), Color.withAlpha(Color.WHITE, 0.15f));
        }

        contentRenderer.render(matrixStack, mouseX - x, mouseY - y);

        matrixStack.popPose();

        return hovered;
    }

    @Override
    public String toString() {
        return String.format("![%s](%s)", title, contentRenderer);
    }

    private float scale(final int maxWidth) {
        return Math.min(1f, maxWidth / (float) contentRenderer.getWidth());
    }

    private int imageWidth(final int maxWidth) {
        return Math.min(maxWidth, contentRenderer.getWidth());
    }

    private int imageHeight(final int maxWidth) {
        return MathHelper.ceil(contentRenderer.getHeight() * scale(maxWidth)) + 4;
    }
}
