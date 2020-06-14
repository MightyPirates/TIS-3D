package li.cil.tis3d.client.manual.segment;

import com.mojang.blaze3d.platform.GlStateManager;
import li.cil.tis3d.api.manual.ImageRenderer;
import li.cil.tis3d.api.manual.InteractiveImageRenderer;
import li.cil.tis3d.client.manual.Document;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public final class RenderSegment extends AbstractSegment implements InteractiveSegment {
    private final Segment parent;
    private final String title;
    private final ImageRenderer imageRenderer;

    private int lastX = 0;
    private int lastY = 0;

    public RenderSegment(final Segment parent, final String title, final ImageRenderer imageRenderer) {
        this.parent = parent;
        this.title = title;
        this.imageRenderer = imageRenderer;
    }

    @Override
    public Segment parent() {
        return this.parent;
    }

    @Override
    public Optional<String> tooltip() {
        if (imageRenderer instanceof InteractiveImageRenderer) {
            return Optional.of(((InteractiveImageRenderer)imageRenderer).getTooltip(title));
        } else {
            return Optional.of(title);
        }
    }

    @Override
    public boolean onMouseClick(final int mouseX, final int mouseY) {
        return imageRenderer instanceof InteractiveImageRenderer &&
            ((InteractiveImageRenderer)imageRenderer).onMouseClick(mouseX - lastX, mouseY - lastY);
    }

    @Override
    public void notifyHover() {
    }

    @Override
    public int nextY(final int indent, final int maxWidth, final TextRenderer renderer) {
        return imageHeight(maxWidth) + ((indent > 0) ? Document.lineHeight(renderer) : 0);
    }

    @Override
    public int nextX(final int indent, final int maxWidth, final TextRenderer renderer) {
        return 0;
    }

    @Override
    public Optional<InteractiveSegment> render(final MatrixStack matrices, final int x, final int y, final int indent, final int maxWidth, final TextRenderer renderer, final int mouseX, final int mouseY) {
        final int width = imageWidth(maxWidth);
        final int height = imageHeight(maxWidth);
        final int xOffset = (maxWidth - width) / 2;
        final int yOffset = 2 + ((indent > 0) ? Document.lineHeight(renderer) : 0);
        final float s = scale(maxWidth);

        lastX = x + xOffset;
        lastY = y + yOffset;

        final Optional<InteractiveSegment> hovered = checkHovered(mouseX, mouseY, x + xOffset, y + yOffset, width, height);

        GlStateManager.color4f(1, 1, 1, 1);
        GlStateManager.pushMatrix();
        GlStateManager.translatef(x + xOffset, y + yOffset, 0);
        GlStateManager.scalef(s, s, s);

        GlStateManager.enableBlend();
        GlStateManager.enableAlphaTest();

        if (hovered.isPresent()) {
            GlStateManager.color4f(1, 1, 1, 0.15f);
            GlStateManager.disableTexture();
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2f(0, 0);
            GL11.glVertex2f(0, imageRenderer.getHeight());
            GL11.glVertex2f(imageRenderer.getWidth(), imageRenderer.getHeight());
            GL11.glVertex2f(imageRenderer.getWidth(), 0);
            GL11.glEnd();
            GlStateManager.enableTexture();
        }

        GlStateManager.color4f(1, 1, 1, 1);

        imageRenderer.render(mouseX - x, mouseY - y);

        GlStateManager.popMatrix();

        return hovered;
    }

    @Override
    public String toString() {
        return String.format("![%s](%s)", title, imageRenderer);
    }

    private float scale(final int maxWidth) {
        return Math.min(1f, maxWidth / (float)imageRenderer.getWidth());
    }

    private int imageWidth(final int maxWidth) {
        return Math.min(maxWidth, imageRenderer.getWidth());
    }

    private int imageHeight(final int maxWidth) {
        return MathHelper.ceil(imageRenderer.getHeight() * scale(maxWidth)) + 4;
    }
}
