package li.cil.manual.api.render;

import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Allows implementing advanced image renderers that react to mouse input and
 * specify customized tooltips.
 * <p>
 * This way you can e.g. disable the default tooltip and render a more advanced
 * one, or render a small GUI on a page.
 */
@OnlyIn(Dist.CLIENT)
public interface InteractiveContentRenderer extends ContentRenderer {
    /**
     * Get a custom tooltip for this image renderer.
     * <p>
     * This can be used to override the original tooltip of an image.
     *
     * @param tooltip the original tooltip of the element.
     * @return the tooltip to use for the element.
     */
    ITextComponent getTooltip(final ITextComponent tooltip);

    /**
     * Called when the mouse is clicked while over this image renderer.
     * <p>
     * This only fires for left-clicks, because right-clicks are reserved for
     * navigating back in the manual.
     * <p>
     * If this returns <tt>false</tt> and the element is a link, the link will
     * be followed. If it returns <tt>true</tt>, it will not.
     *
     * @return whether the click was handled.
     */
    default boolean mouseClicked() {
        return false;
    }
}
