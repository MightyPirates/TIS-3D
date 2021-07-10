package li.cil.manual.api;

import li.cil.manual.api.prefab.renderer.MinecraftFontRenderer;
import li.cil.manual.api.render.FontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Style definition used for rendering manuals.
 */
@OnlyIn(Dist.CLIENT)
public interface ManualStyle {
    /**
     * Default implementation of a manual style.
     */
    ManualStyle DEFAULT = new ManualStyle() {
    };

    /**
     * The text color to use when rendering regular text.
     *
     * @return the regular text color.
     */
    default int getRegularTextColor() {
        return 0xFF333333;
    }

    /**
     * The text color to use when rendering mono-spaced text.
     *
     * @return the monospace text color.
     */
    default int getMonospaceTextColor() {
        return 0xFF404D80;
    }

    /**
     * Regular color of links.
     *
     * @return color of links.
     */
    default int getRegularLinkColor() {
        return 0xFF333399;
    }

    /**
     * Color of links while hovered by the cursor.
     *
     * @return hovered link color.
     */
    default int getHoveredLinkColor() {
        return 0xFF6666CC;
    }

    /**
     * Regular color of  dead links.
     *
     * @return color of dead links.
     */
    default int getRegularDeadLinkColor() {
        return 0xFF993333;
    }

    /**
     * Color of dead links while hovered by the cursor.
     *
     * @return hovered dead link color.
     */
    default int getHoveredDeadLinkColor() {
        return 0xFFCC6666;
    }

    /**
     * The font to use for rendering regular text.
     *
     * @return the regular font.
     */
    default FontRenderer getRegularFont() {
        return new MinecraftFontRenderer(Minecraft.getInstance().font);
    }

    /**
     * The font to use for rendering mono-spaced text.
     *
     * @return the monospace font.
     */
    default FontRenderer getMonospaceFont() {
        return new MinecraftFontRenderer(Minecraft.getInstance().font);
    }

    /**
     * The line height for lines in the manual.
     * <p>
     * Fonts will be scaled to match their inherent line height to this value.
     *
     * @return the regular line height.
     */
    default int getLineHeight() {
        return getRegularFont().lineHeight();
    }

    /**
     * Whether to show the url of links as their tooltip.
     *
     * @return whether to show link tooltips.
     */
    default boolean showLinkTooltip() {
        return true;
    }

    /**
     * The sound to play when the current page in the manual is changed,
     * e.g. due to a {@link Tab} or a link being clicked.
     *
     * @return the page changed sound.
     */
    default SoundEvent getPageChangeSound() {
        return SoundEvents.BOOK_PAGE_TURN;
    }
}
