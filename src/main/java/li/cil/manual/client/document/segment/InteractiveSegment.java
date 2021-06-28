package li.cil.manual.client.document.segment;

import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Optional;

/**
 * Segments that can react to mouse presence and input.
 * <p>
 * The currently hovered interactive segment is picked in the render process
 * and returned there. Calling code can then decide whether to render the
 * segment's tooltip, for example. It should also notice the currently hovered
 * segment when a left-click occurs.
 */
@OnlyIn(Dist.CLIENT)
public interface InteractiveSegment extends Segment {
    /**
     * The tooltip that should be displayed when this segment is being hovered.
     *
     * @return the tooltip for this interactive segment, if any.
     */
    default Optional<ITextComponent> getTooltip() {
        return Optional.empty();
    }

    /**
     * Should be called by whatever is rendering the document when a left mouse
     * click occurs.
     * <p>
     * The mouse coordinates are expected to be in the same frame of reference as
     * the document.
     *
     * @return whether the click was processed (true) or ignored (false).
     */
    boolean mouseClicked();

    /**
     * Sets whether this interactive segment is the one currently hovered in its document.
     * <p>
     * Useful to track when the segment was last hovered, e.g. this is used for link highlighting.
     *
     * @param value {@code true} when hovered, {@code false} otherwise.
     */
    default void setMouseHovered(final boolean value) {
    }
}
