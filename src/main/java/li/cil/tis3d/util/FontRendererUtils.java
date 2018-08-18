package li.cil.tis3d.util;

import li.cil.tis3d.common.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

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
    public static void addStringToTooltip(final String info, final List<ITextComponent> tooltip) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) {
            final FontRenderer fontRenderer = mc.fontRenderer;
            tooltip.addAll(fontRenderer.listFormattedStringToWidth(info, Constants.MAX_TOOLTIP_WIDTH).stream().map(TextComponentString::new).collect(Collectors.toList()));
        } else {
            tooltip.add(new TextComponentString(info));
        }
    }

    // --------------------------------------------------------------------- //

    private FontRendererUtils() {
    }
}
