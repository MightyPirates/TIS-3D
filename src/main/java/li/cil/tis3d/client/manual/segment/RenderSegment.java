package li.cil.tis3d.client.manual.segment;

import com.mojang.blaze3d.matrix.MatrixStack;
import li.cil.tis3d.api.manual.ImageRenderer;
import li.cil.tis3d.api.manual.InteractiveImageRenderer;
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
    private final ImageRenderer imageRenderer;

    private int lastX = 0;
    private int lastY = 0;

    public RenderSegment(final Segment parent, final ITextComponent title, final ImageRenderer imageRenderer) {
        this.parent = parent;
        this.title = title;
        this.imageRenderer = imageRenderer;
    }

    @Override
    public Segment parent() {
        return this.parent;
    }

    @Override
    public Optional<ITextComponent> tooltip() {
        if (imageRenderer instanceof InteractiveImageRenderer) {
            return Optional.of(((InteractiveImageRenderer) imageRenderer).getTooltip(title));
        } else {
            return Optional.of(title);
        }
    }

    @Override
    public boolean onMouseClick(final double mouseX, final double mouseY) {
        return imageRenderer instanceof InteractiveImageRenderer &&
               ((InteractiveImageRenderer) imageRenderer).onMouseClick(mouseX - lastX, mouseY - lastY);
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

        matrixStack.push();
        matrixStack.translate(x + xOffset, y + yOffset, 0);
        matrixStack.scale(s, s, s);

        if (hovered.isPresent()) {
            Screen.fill(matrixStack, 0, 0, imageRenderer.getWidth(), imageRenderer.getHeight(), Color.withAlpha(Color.WHITE, 0.15f));
        }

        imageRenderer.render(matrixStack, mouseX - x, mouseY - y);

        matrixStack.pop();

        return hovered;
    }

    @Override
    public String toString() {
        return String.format("![%s](%s)", title, imageRenderer);
    }

    private float scale(final int maxWidth) {
        return Math.min(1f, maxWidth / (float) imageRenderer.getWidth());
    }

    private int imageWidth(final int maxWidth) {
        return Math.min(maxWidth, imageRenderer.getWidth());
    }

    private int imageHeight(final int maxWidth) {
        return MathHelper.ceil(imageRenderer.getHeight() * scale(maxWidth)) + 4;
    }
}
