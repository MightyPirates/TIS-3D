package li.cil.tis3d.util;

import li.cil.tis3d.common.Constants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringRenderable;
import net.minecraft.text.Text;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility methods based on the default Minecraft font renderer.
 */
@Environment(EnvType.CLIENT)
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
    public static void addStringToTooltip(final String info, final List<Text> tooltip) {
        final MinecraftClient mc = MinecraftClient.getInstance();
        //if (mc != null) {
            //final TextRenderer fontRenderer = mc.textRenderer;
            //tooltip.addAll(fontRenderer.wrapLines(info, Constants.MAX_TOOLTIP_WIDTH).stream().map(LiteralText::new).collect(Collectors.toList()));
        //} else {
            tooltip.add(new LiteralText(info));
        //}
    }

    /**
     * Tries to format the specified tooltip string so it does not exceed our maximum tooltip width
     * and add it to the specified tooltip list. If no font renderer is available, add the info as is.
     * The latter may happen when some other mod wants to get an items tooltip before the font renderer
     * is available.
     *
     * @param info    the info to add to the tooltip.
     * @param tooltip the tooltip to add the info to.
     */
    public static void addStringToTooltip2(final String info, final List<StringRenderable> tooltip) {
        final MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null) {
            final TextRenderer fontRenderer = mc.textRenderer;
            tooltip.addAll(fontRenderer.wrapLines(StringRenderable.plain(info), Constants.MAX_TOOLTIP_WIDTH).stream().collect(Collectors.toList()));
        } else {
            tooltip.add(StringRenderable.plain(info));
        }
    }

    public static void addStringListToTooltip(final List<String> infos, final List<StringRenderable> tooltip) {
        tooltip.addAll(infos.stream().map(StringRenderable::plain).collect(Collectors.toList()));
    }

    public static StringRenderable translate(final String key) {
        return StringRenderable.plain(I18n.translate(key));
    }

    // --------------------------------------------------------------------- //

    private FontRendererUtils() {
    }
}
