package li.cil.tis3d.client.manual.segment;

import com.mojang.blaze3d.platform.GlStateManager;
import li.cil.tis3d.client.manual.Document;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Environment(EnvType.CLIENT)
public class TextSegment extends BasicTextSegment {
    private final Segment parent;
    private final String text;

    public TextSegment(@Nullable final Segment parent, final String text) {
        this.parent = parent;
        this.text = text;
    }

    @Override
    @Nullable
    public Segment parent() {
        return parent;
    }

    @Override
    protected String text() {
        return text;
    }

    @Override
    public Optional<InteractiveSegment> render(final int x, final int y, final int indent, final int maxWidth, final TextRenderer renderer, final int mouseX, final int mouseY) {
        int currentX = x + indent;
        int currentY = y;
        String chars = text;
        if (indent == 0) {
            chars = chars.substring(indexOfFirstNonWhitespace(chars));
        }
        final int wrapIndent = computeWrapIndent(renderer);
        int numChars = maxChars(chars, maxWidth - indent, maxWidth - wrapIndent, renderer);
        Optional<InteractiveSegment> hovered = Optional.empty();
        final float scale = resolvedScale();
        final String format = resolvedFormat();
        final int color = resolvedColor();
        final Optional<InteractiveSegment> interactive = resolvedInteractive();
        while (chars.length() > 0) {
            final String part = chars.substring(0, numChars);
            if (!hovered.isPresent()) {
                final int cx = currentX;
                final int cy = currentY;
                hovered = interactive.flatMap(segment -> segment.checkHovered(mouseX, mouseY, cx, cy, stringWidth(part, renderer), (int)(Document.lineHeight(renderer) * scale)));
            }
            GlStateManager.color4f(0f, 0f, 0f, 1);
            GlStateManager.pushMatrix();
            GlStateManager.translatef(currentX, currentY, 0);
            GlStateManager.scalef(scale, scale, scale);
            GlStateManager.translatef(-currentX, -currentY, 0);
            //~ renderer.draw(format + part, currentX, currentY, color);
            GlStateManager.popMatrix();
            currentX = x + wrapIndent;
            currentY += lineHeight(renderer);
            chars = chars.substring(numChars);
            chars = chars.substring(indexOfFirstNonWhitespace(chars));
            numChars = maxChars(chars, maxWidth - wrapIndent, maxWidth - wrapIndent, renderer);
        }

        return hovered;
    }

    @Override
    public Iterable<Segment> refine(final Pattern pattern, final SegmentRefiner factory) {
        final List<Segment> result = new ArrayList<>();

        // Keep track of last matches end, to generate plain text segments.
        int textStart = 0;
        final Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            // Create segment for leading plain text.
            if (matcher.start() > textStart) {
                result.add(new TextSegment(this, text.substring(textStart, matcher.start())));
            }
            textStart = matcher.end();

            // Create segment for formatted text.
            result.add(factory.refine(this, matcher));
        }

        // Create segment for remaining plain text.
        if (textStart == 0) {
            result.add(this);
        } else if (textStart < text.length()) {
            result.add(new TextSegment(this, text.substring(textStart)));
        }
        return result;
    }

    // ----------------------------------------------------------------------- //

    @Override
    protected int lineHeight(final TextRenderer renderer) {
        return (int)(super.lineHeight(renderer) * resolvedScale());
    }

    @Override
    protected int stringWidth(final String s, final TextRenderer renderer) {
        return (int)(renderer.getWidth(resolvedFormat() + s) * resolvedScale());
    }

    // ----------------------------------------------------------------------- //

    protected Optional<Integer> color() {
        return Optional.empty();
    }

    protected Optional<Float> scale() {
        return Optional.empty();
    }

    protected String format() {
        return "";
    }

    private int resolvedColor() {
        return color().orElseGet(this::parentColor);
    }

    private int parentColor() {
        final Segment parent = parent();
        if (parent instanceof TextSegment) {
            return ((TextSegment)parent).resolvedColor();
        } else {
            return 0x333333;
        }
    }

    private float resolvedScale() {
        return scale().orElseGet(this::parentScale);
    }

    private float parentScale() {
        final Segment parent = parent();
        if (parent instanceof TextSegment) {
            return scale().orElse(1f) * ((TextSegment)parent).resolvedScale();
        } else {
            return 1f;
        }
    }

    private String resolvedFormat() {
        final Segment parent = parent();
        if (parent instanceof TextSegment) {
            return ((TextSegment)parent).resolvedFormat() + format();
        } else {
            return format();
        }
    }

    private Optional<InteractiveSegment> resolvedInteractive() {
        if (this instanceof InteractiveSegment) {
            return Optional.of((InteractiveSegment)this);
        } else {
            final Segment parent = parent();
            if (parent instanceof TextSegment) {
                return ((TextSegment)parent).resolvedInteractive();
            } else {
                return Optional.empty();
            }
        }
    }
}
