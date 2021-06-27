package li.cil.manual.client.document.segment;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Optional;
import java.util.regex.Pattern;

@OnlyIn(Dist.CLIENT)
public interface Segment {
    /**
     * The root segment, i.e. the parent defining the full line this segment is a part of.
     * <p>
     * For the root element this will be the element itself.
     *
     * @return the root segment.
     */
    Segment getLineRoot();

    /**
     * Parent segment, i.e. the segment this segment was refined from.
     * Each line starts as a TextSegment that is refined based into segments
     * based on the handled formatting rules / patterns.
     *
     * @return the parent segment.
     */
    Optional<Segment> getParent();

    /**
     * The render height of this segment in a single line, given the specified indent
     * and document width.
     * <p>
     * This extra context is necessary because some elements will scale dynamically with
     * available space, such as texture render segments.
     *
     * @param indent        the current indentation.
     * @param documentWidth the width of the containing document.
     * @return the height of the segment.
     */
    int getLineHeight(final int indent, final int documentWidth);

    /**
     * Gets the next sibling segment and the indent and y position to start rendering
     * the sibling at, given the current indent and document width, as this segment's
     * size and wrapping behaviour may depend on the context it is rendered in.
     *
     * @param segmentX      the current indentation.
     * @param lineHeight
     * @param documentWidth the width of the containing document.
     * @return the next segment in the linked list of segments.
     */
    NextSegmentInfo getNext(final int segmentX, final int lineHeight, final int documentWidth);

    /**
     * Render the segment at the specified coordinates with the specified
     * properties.
     *
     * @param matrixStack   the current matrix stack.
     * @param segmentX      the current indentation.
     * @param lineHeight    the maximum height of the current line.
     * @param documentWidth the maximum width of the document.
     * @param mouseX        the x mouse position.
     * @param mouseY        the y mouse position.
     * @return the hovered interactive segment, if any.
     */
    Optional<InteractiveSegment> render(final MatrixStack matrixStack, final int segmentX, final int lineHeight, final int documentWidth, final int mouseX, final int mouseY);

    // ----------------------------------------------------------------------- //

    /**
     * Used during construction, checks a segment for inner segments.
     *
     * @param pattern the regex pattern used for refinement.
     * @param refiner the callback for successful matches.
     * @return a list of child segments.
     */
    Iterable<Segment> refine(final Pattern pattern, final SegmentRefiner refiner);

    void setNext(final Segment segment);
}
