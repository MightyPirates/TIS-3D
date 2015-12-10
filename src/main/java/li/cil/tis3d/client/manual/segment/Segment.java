package li.cil.tis3d.client.manual.segment;

import net.minecraft.client.gui.FontRenderer;

import java.util.Optional;
import java.util.regex.Pattern;

public interface Segment {
    /**
     * Parent segment, i.e. the segment this segment was refined from.
     * Each line starts as a TextSegment that is refined based into segments
     * based on the handled formatting rules / patterns.
     */
    Segment parent();

    /**
     * The root segment, i.e. the original parent of this segment.
     */
    Segment root();

    /**
     * Get the X coordinate at which to render the next segment.
     * <p>
     * For flowing/inline segments this will be to the right of the last line
     * this segment renders, for block segments it will be at the start of
     * the next line below this segment.
     * <p>
     * The coordinates in this context are relative to (0,0).
     */
    int nextX(int indent, int maxWidth, FontRenderer renderer);

    /**
     * Get the Y coordinate at which to render the next segment.
     * <p>
     * For flowing/inline segments this will be the same level as the last line
     * this segment renders, unless it's the last segment on its line. For block
     * segments and last-on-line segments this will be the next line after.
     * <p>
     * The coordinates in this context are relative to (0,0).
     */
    int nextY(int indent, int maxWidth, FontRenderer renderer);

    /**
     * Render the segment at the specified coordinates with the specified
     * properties.
     */
    Optional<InteractiveSegment> render(final int x, final int y, final int indent, final int maxWidth, final FontRenderer renderer, final int mouseX, final int mouseY);

    Iterable<String> renderAsText();

    // ----------------------------------------------------------------------- //

    /**
     * Used during construction, checks a segment for inner segments.
     */
    Iterable<Segment> refine(final Pattern pattern, final SegmentRefiner refiner);

    /**
     * Set after construction of document, used for formatting, specifically
     * to compute the height for last segment on a line (to force a new line).
     */
    Segment next();

    void setNext(Segment segment);

    /**
     * Utility method to check if the segment is the last on a line.
     */
    boolean isLast();
}
