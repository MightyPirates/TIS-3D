package li.cil.tis3d.client.manual.segment;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.regex.Pattern;

@Environment(EnvType.CLIENT)
public interface Segment {
    /**
     * Parent segment, i.e. the segment this segment was refined from.
     * Each line starts as a TextSegment that is refined based into segments
     * based on the handled formatting rules / patterns.
     *
     * @return the parent segment.
     */
    @Nullable
    Segment parent();

    /**
     * The root segment, i.e. the original parent of this segment.
     *
     * @return the root segment.
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
     *
     * @param indent   the current indentation.
     * @param maxWidth the maximum width of the document.
     * @param renderer the font renderer used.
     * @return the x position of the next segment.
     */
    int nextX(final int indent, final int maxWidth, final TextRenderer renderer);

    /**
     * Get the Y coordinate at which to render the next segment.
     * <p>
     * For flowing/inline segments this will be the same level as the last line
     * this segment renders, unless it's the last segment on its line. For block
     * segments and last-on-line segments this will be the next line after.
     * <p>
     * The coordinates in this context are relative to (0,0).
     *
     * @param indent   the current indentation.
     * @param maxWidth the maximum width of the document.
     * @param renderer the font renderer used.
     * @return the y position of the next segment.
     */
    int nextY(final int indent, final int maxWidth, final TextRenderer renderer);

    /**
     * Render the segment at the specified coordinates with the specified
     * properties.
     *
     * @param matrices the transformation stack.
     * @param x        the x position to render at.
     * @param y        the y position to render at.
     * @param indent   the current indentation.
     * @param maxWidth the maximum width of the document.
     * @param renderer the font renderer to use.
     * @param mouseX   the x mouse position.
     * @param mouseY   the y mouse position.
     * @return the hovered interactive segment, if any.
     */
    Optional<InteractiveSegment> render(final MatrixStack matrices, final int x, final int y, final int indent, final int maxWidth, final TextRenderer renderer, final int mouseX, final int mouseY);

    // ----------------------------------------------------------------------- //

    /**
     * Used during construction, checks a segment for inner segments.
     *
     * @param pattern the regex pattern used for refinement.
     * @param refiner the callback for successful matches.
     * @return a list of child segments.
     */
    Iterable<Segment> refine(final Pattern pattern, final SegmentRefiner refiner);

    /**
     * Set after construction of document, used for formatting, specifically
     * to compute the height for last segment on a line (to force a new line).
     *
     * @return the next segment in the linked list of segments.
     */
    @Nullable
    Segment next();

    void setNext(final Segment segment);
}
