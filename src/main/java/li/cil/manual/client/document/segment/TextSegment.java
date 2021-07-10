package li.cil.manual.client.document.segment;

import com.mojang.blaze3d.matrix.MatrixStack;
import li.cil.manual.api.ManualModel;
import li.cil.manual.api.ManualStyle;
import li.cil.manual.api.render.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@OnlyIn(Dist.CLIENT)
public class TextSegment extends AbstractSegment {
    private static final char[] BREAKS = {' ', '.', ',', ':', ';', '!', '?', '_', '=', '-', '+', '*', '/', '\\'};
    private static final CharSequence[] LISTS = {"- ", "* "};

    // ----------------------------------------------------------------------- //

    private final String text;
    private final List<TextBlock> blockCache = new ArrayList<>();
    private CacheKey blockCacheKey;
    private NextSegmentInfo nextCache;
    private CacheKey nextCacheKey;

    // ----------------------------------------------------------------------- //

    public TextSegment(final ManualModel manual, final ManualStyle style, @Nullable final Segment parent, final String text) {
        super(manual, style, parent);
        this.text = text;
    }

    // ----------------------------------------------------------------------- //

    @Override
    public int getLineHeight(final int indent, final int documentWidth) {
        return getLineHeight();
    }

    @Override
    public NextSegmentInfo getNext(final int segmentX, final int lineHeight, final int documentWidth) {
        final CacheKey cacheKey = new CacheKey(segmentX, lineHeight, documentWidth);
        if (!Objects.equals(cacheKey, nextCacheKey)) {
            nextCache = new NextSegmentInfo(next);
            forEachBlock(segmentX, lineHeight, documentWidth, block -> {
                nextCache.absoluteX = block.x + getStringWidth(block.chars);
                nextCache.relativeY = block.y;
            });

            // If the next segment belongs to a different hierarchy we force it to a new line.
            // This is mainly for stuff like lists.
            if (next != null && next.getLineRoot() != getLineRoot()) {
                nextCache.absoluteX = 0;
                if (nextCache.relativeY == 0) {
                    nextCache.relativeY = Math.max(lineHeight, getLineHeight());
                } else {
                    nextCache.relativeY += getLineHeight();
                }
            }

            nextCacheKey = cacheKey;
        }

        return nextCache;
    }

    @Override
    public Optional<InteractiveSegment> render(final MatrixStack matrixStack, final int segmentX, final int lineHeight, final int documentWidth, final int mouseX, final int mouseY) {
        final String format = getFormat();
        final float scale = getFontScale() * getScale();
        final int color = getColor();

        final Optional<InteractiveSegment> interactive = getInteractiveParent();
        final ObjectReference<Optional<InteractiveSegment>> hovered = new ObjectReference<>(Optional.empty());

        final BufferBuilder builder = Tessellator.getInstance().getBuilder();
        final IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.immediate(builder);

        forEachBlock(segmentX, lineHeight, documentWidth, block -> {
            final int blockWidth = getStringWidth(block.chars);
            final int blockHeight = getLineHeight();
            if (!hovered.value.isPresent() &&
                mouseX >= block.x && mouseX <= block.x + blockWidth &&
                mouseY >= block.y && mouseY <= block.y + blockHeight) {
                hovered.value = interactive;
            }

            matrixStack.pushPose();
            matrixStack.translate(block.x, block.y, 0);
            matrixStack.scale(scale, scale, scale);

            getFont().drawBatch(matrixStack, buffer, format + block.chars, color);

            matrixStack.popPose();
        });

        buffer.endBatch();

        return hovered.value;
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
                result.add(new TextSegment(manual, style, this, text.substring(textStart, matcher.start())));
            }
            textStart = matcher.end();

            // Create segment for formatted text.
            result.add(factory.refine(manual, style, this, matcher));
        }

        // Create segment for remaining plain text.
        if (textStart == 0) {
            result.add(this);
        } else if (textStart < text.length()) {
            result.add(new TextSegment(manual, style, this, text.substring(textStart)));
        }
        return result;
    }

    @Override
    public String toString() {
        return text;
    }

    // ----------------------------------------------------------------------- //

    protected boolean isIgnoringLeadingWhitespace() {
        return true;
    }

    protected FontRenderer getFont() {
        return style.getRegularFont();
    }

    protected int getColor() {
        return tryGetFromParent(style.getRegularTextColor(), TextSegment::getColor);
    }

    protected float getScale() {
        return tryGetFromParent(1f, TextSegment::getScale);
    }

    protected String getFormat() {
        return tryGetFromParent("", TextSegment::getFormat);
    }

    protected int getLineHeight() {
        return (int) ((getFont().lineHeight() + 1) * getFontScale() * getScale());
    }

    // ----------------------------------------------------------------------- //

    private float getFontScale() {
        return style.getLineHeight() / (float) getFont().lineHeight();
    }

    private int getStringWidth(final CharSequence string) {
        return (int) (getFont().width(getFormat() + string) * getFontScale() * getScale());
    }

    private void forEachBlock(final int segmentX, final int lineHeight, final int documentWidth, final Consumer<TextBlock> blockConsumer) {
        final CacheKey cacheKey = new CacheKey(segmentX, lineHeight, documentWidth);
        if (!Objects.equals(cacheKey, blockCacheKey)) {
            blockCache.clear();

            String chars = text;
            if (isIgnoringLeadingWhitespace() && segmentX == 0) {
                chars = chars.substring(indexOfFirstNonWhitespace(chars));
            }

            final int wrappedIndent = computeWrappedIndent();
            int currentX = segmentX;
            int currentY = 0;

            int charCount = computeCharsFittingOnLine(chars, documentWidth - currentX, documentWidth - wrappedIndent);
            while (chars.length() > 0) {
                final String blockChars = chars.substring(0, charCount);
                blockCache.add(new TextBlock(
                    currentX,
                    currentY,
                    blockChars
                ));

                currentX = wrappedIndent;
                if (currentY == 0) {
                    currentY = Math.max(lineHeight, getLineHeight());
                } else {
                    currentY += getLineHeight();
                }

                chars = chars.substring(charCount);
                chars = chars.substring(indexOfFirstNonWhitespace(chars));
                charCount = computeCharsFittingOnLine(chars, documentWidth - currentX, documentWidth - wrappedIndent);
            }

            blockCacheKey = cacheKey;
        }

        for (final TextBlock block : blockCache) {
            blockConsumer.accept(block);
        }
    }

    private static int indexOfFirstNonWhitespace(final String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return i;
            }
        }
        return s.length();
    }

    private int computeCharsFittingOnLine(final String string, final int remainingLineWidth, final int documentWidth) {
        final int fullWidth = getStringWidth(string);

        int count = 0;
        int lastBreak = -1;
        while (count < string.length()) {
            final int nextLargerWidth = getStringWidth(string.substring(0, count + 1));
            final boolean exceedsLineLength = nextLargerWidth >= remainingLineWidth;
            if (exceedsLineLength) {
                final boolean mayUseFullLine = remainingLineWidth == documentWidth;
                final boolean canFitInLine = fullWidth <= documentWidth;
                final boolean matchesFullLine = fullWidth == documentWidth;
                if (lastBreak >= 0) {
                    return lastBreak + 1; // Can do a soft split.
                }
                if (mayUseFullLine && matchesFullLine) {
                    return string.length(); // Special case for exact match.
                }
                if (canFitInLine && !mayUseFullLine) {
                    return 0; // Wrap line, use next line.
                }
                return count; // Gotta split hard.
            }
            if (ArrayUtils.contains(BREAKS, string.charAt(count))) {
                lastBreak = count;
            }
            count += 1;
        }
        return count;
    }

    private <T> T tryGetFromParent(final T defaultValue, final Function<TextSegment, T> getter) {
        final Optional<Segment> parent = getParent();
        if (parent.isPresent() && parent.get() instanceof TextSegment) {
            return getter.apply((TextSegment) parent.get());
        } else {
            return defaultValue;
        }
    }

    private Optional<InteractiveSegment> getInteractiveParent() {
        Optional<Segment> segment = Optional.of(this);
        while (segment.isPresent()) {
            if (segment.get() instanceof InteractiveSegment) {
                return segment.map(s -> (InteractiveSegment) s);
            }
            segment = segment.get().getParent();
        }
        return Optional.empty();
    }

    private TextSegment getRootTextSegment() {
        TextSegment textSegment = this;
        Optional<Segment> parent = getParent();
        while (parent.isPresent() && parent.get() instanceof TextSegment) {
            textSegment = (TextSegment) parent.get();
            parent = parent.get().getParent();
        }
        return textSegment;
    }

    private int computeWrappedIndent() {
        final TextSegment textSegment = getRootTextSegment();
        final CharSequence rootPrefix = textSegment.text.subSequence(0, Math.min(2, textSegment.text.length()));
        return (ArrayUtils.contains(LISTS, rootPrefix)) ? getFont().width(rootPrefix) : 0;
    }

    // ----------------------------------------------------------------------- //

    private static final class ObjectReference<T> {
        public T value;

        public ObjectReference(final T value) {
            this.value = value;
        }
    }

    private static final class CacheKey {
        private final int segmentX;
        private final int lineHeight;
        private final int documentWidth;

        public CacheKey(final int segmentX, final int lineHeight, final int documentWidth) {
            this.segmentX = segmentX;
            this.lineHeight = lineHeight;
            this.documentWidth = documentWidth;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final CacheKey that = (CacheKey) o;
            return segmentX == that.segmentX && lineHeight == that.lineHeight && documentWidth == that.documentWidth;
        }

        @Override
        public int hashCode() {
            return Objects.hash(segmentX, lineHeight, documentWidth);
        }
    }

    private static final class TextBlock {
        public final int x;
        public final int y;
        public final String chars;

        public TextBlock(final int x, final int y, final String chars) {
            this.x = x;
            this.y = y;
            this.chars = chars;
        }
    }
}
