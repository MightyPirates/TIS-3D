package li.cil.tis3d.client.manual.segment;

import com.google.common.collect.ImmutableSet;
import li.cil.tis3d.client.manual.Document;
import net.minecraft.client.gui.FontRenderer;

import java.util.Set;

abstract class BasicTextSegment extends AbstractSegment implements Segment {
    private static final Set<Character> BREAKS = ImmutableSet.of(' ', '.', ',', ':', ';', '!', '?', '_', '=', '-', '+', '*', '/', '\\');
    private static final Set<String> LISTS = ImmutableSet.of("- ", "* ");

    private String rootPrefix = null; // Lazily initialized.

    private String getRootPrefix() {
        if (rootPrefix == null) {
            final TextSegment segment = (TextSegment) root();
            rootPrefix = segment.text().substring(0, Math.min(2, segment.text().length()));
        }
        return rootPrefix;
    }

    // ----------------------------------------------------------------------- //

    @Override
    public int nextX(final int indent, final int maxWidth, final FontRenderer renderer) {
        if (isLast()) {
            return 0;
        }
        int currentX = indent;
        String chars = text();
        if (ignoreLeadingWhitespace() && indent == 0) {
            chars = chars.substring(indexOfFirstNonWhitespace(chars));
        }
        final int wrapIndent = computeWrapIndent(renderer);
        int numChars = maxChars(chars, maxWidth - indent, maxWidth - wrapIndent, renderer);
        while (chars.length() > numChars) {
            chars = chars.substring(numChars);
            chars = chars.substring(indexOfFirstNonWhitespace(chars));
            numChars = maxChars(chars, maxWidth - wrapIndent, maxWidth - wrapIndent, renderer);
            currentX = wrapIndent;
        }
        return currentX + stringWidth(chars, renderer);
    }

    @Override
    public int nextY(final int indent, final int maxWidth, final FontRenderer renderer) {
        int lines = 0;
        String chars = text();
        if (ignoreLeadingWhitespace() && indent == 0) {
            chars = chars.substring(indexOfFirstNonWhitespace(chars));
        }
        final int wrapIndent = computeWrapIndent(renderer);
        int numChars = maxChars(chars, maxWidth - indent, maxWidth - wrapIndent, renderer);
        while (chars.length() > numChars) {
            lines += 1;
            chars = chars.substring(numChars);
            chars = chars.substring(indexOfFirstNonWhitespace(chars));
            numChars = maxChars(chars, maxWidth - wrapIndent, maxWidth - wrapIndent, renderer);
        }
        if (isLast()) {
            lines += 1;
        }
        return lines * lineHeight(renderer);
    }

    @Override
    public String toString() {
        return text();
    }

    // ----------------------------------------------------------------------- //

    protected abstract String text();

    protected boolean ignoreLeadingWhitespace() {
        return true;
    }

    protected int lineHeight(final FontRenderer renderer) {
        return Document.lineHeight(renderer);
    }

    protected abstract int stringWidth(String s, FontRenderer renderer);

    protected int maxChars(final String s, final int maxWidth, final int maxLineWidth, final FontRenderer renderer) {
        int pos = -1;
        int lastBreak = -1;
        final int fullWidth = stringWidth(s, renderer);
        while (pos < s.length()) {
            pos += 1;
            final int width = stringWidth(s.substring(0, pos), renderer);
            final boolean exceedsLineLength = width >= maxWidth;
            if (exceedsLineLength) {
                final boolean mayUseFullLine = maxWidth == maxLineWidth;
                final boolean canFitInLine = fullWidth <= maxLineWidth;
                final boolean matchesFullLine = fullWidth == maxLineWidth;
                if (lastBreak >= 0) {
                    return lastBreak + 1; // Can do a soft split.
                }
                if (mayUseFullLine && matchesFullLine) {
                    return s.length(); // Special case for exact match.
                }
                if (canFitInLine && !mayUseFullLine) {
                    return 0; // Wrap line, use next line.
                }
                return pos - 1; // Gotta split hard.
            }
            if (pos < s.length() && BREAKS.contains(s.charAt(pos))) {
                lastBreak = pos;
            }
        }
        return pos;
    }

    protected int computeWrapIndent(final FontRenderer renderer) {
        return (LISTS.contains(getRootPrefix())) ? renderer.getStringWidth(getRootPrefix()) : 0;
    }

    // ----------------------------------------------------------------------- //

    private boolean isLast() {
        final Segment next = next();
        return next == null || root() != next.root();
    }

    // ----------------------------------------------------------------------- //

    protected static int indexOfFirstNonWhitespace(final String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return i;
            }
        }
        return s.length();
    }
}
