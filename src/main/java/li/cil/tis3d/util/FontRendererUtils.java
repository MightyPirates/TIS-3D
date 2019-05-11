package li.cil.tis3d.util;

import li.cil.tis3d.common.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
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
    public static void addStringToTooltip(final String info, final List<Component> tooltip) {
        final MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null) {
            final TextRenderer fontRenderer = mc.textRenderer;
            tooltip.addAll(fontRenderer.wrapStringToWidthAsList(info, Constants.MAX_TOOLTIP_WIDTH).stream().map(TextComponent::new).collect(Collectors.toList()));
        } else {
            tooltip.add(new TextComponent(info));
        }
    }

    // --------------------------------------------------------------------- //

    private FontRendererUtils() {
    }
}
