package li.cil.tis3d.util;

import li.cil.tis3d.common.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.FontRenderer;
import net.minecraft.text.StringTextComponent;
import net.minecraft.text.TextComponent;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility methods based on the default Minecraft font renderer.
 */
public final class FontRendererUtils {
    /**
     * Tries to format the specified tooltip string so it does not exceed our maximum tooltip width
     * and add it to the specified tooltip list. If no font renderer is available, add the info as is.
     * The latter may happen when some other mod wants to get an items tooltip before the font renderer
     * is available.
     *
     * @param info    the info to add to the tooltip.
     * @param tooltip the tooltip to add the info to.
     */
    public static void addStringToTooltip(final String info, final List<TextComponent> tooltip) {
        final MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null) {
            final FontRenderer fontRenderer = mc.fontRenderer;
            tooltip.addAll(fontRenderer.wrapStringToWidthAsList(info, Constants.MAX_TOOLTIP_WIDTH).stream().map(StringTextComponent::new).collect(Collectors.toList()));
        } else {
            tooltip.add(new StringTextComponent(info));
        }
    }

    // --------------------------------------------------------------------- //

    private FontRendererUtils() {
    }
}
