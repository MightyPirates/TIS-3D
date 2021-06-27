package li.cil.manual.client.document.segment;

import li.cil.manual.api.Manual;
import li.cil.manual.api.Style;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.net.URI;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public final class LinkSegment extends TextSegment implements InteractiveSegment {
    private static final int NORMAL_COLOR = 0x333399;
    private static final int NORMAL_COLOR_HOVER = 0x6666CC;
    private static final int ERROR_COLOR = 0x993333;
    private static final int ERROR_COLOR_HOVER = 0xCC6666;
    private static final int FADE_TIME = 500;

    private final String url;
    private boolean isLinkValid; // Lazy computation.
    private boolean isLinkValidInitialized;
    private long lastHovered = System.currentTimeMillis() - FADE_TIME;

    public LinkSegment(final Manual manual, final Style style, final Segment parent, final String text, final String url) {
        super(manual, style, parent, text);
        this.url = url;
    }

    private boolean isLinkValid() {
        if (!isLinkValidInitialized) {
            isLinkValid = (url.startsWith("http://") || url.startsWith("https://")) ||
                          manual.contentFor(manual.resolve(url)).isPresent();
            isLinkValidInitialized = true;
        }
        return isLinkValid;
    }

    @Override
    protected int getColor() {
        final int color, hoverColor;
        if (isLinkValid()) {
            color = NORMAL_COLOR;
            hoverColor = NORMAL_COLOR_HOVER;
        } else {
            color = ERROR_COLOR;
            hoverColor = ERROR_COLOR_HOVER;
        }

        final int timeSinceHover = (int) (System.currentTimeMillis() - lastHovered);
        if (timeSinceHover > FADE_TIME) {
            return color;
        } else {
            return fadeColor(hoverColor, color, timeSinceHover / (float) FADE_TIME);
        }
    }

    @Override
    public Optional<ITextComponent> getTooltip() {
        return Optional.of(new StringTextComponent(url));
    }

    @Override
    public boolean mouseClicked() {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            handleUrl(url);
        } else {
            manual.push(manual.resolve(url));
        }
        return true;
    }

    @Override
    public void mouseHovered() {
        lastHovered = System.currentTimeMillis();
    }

    private static int fadeColor(final int color1, final int color2, final float t) {
        final int r1 = (color1 >>> 16) & 0xFF;
        final int g1 = (color1 >>> 8) & 0xFF;
        final int b1 = color1 & 0xFF;
        final int r2 = (color2 >>> 16) & 0xFF;
        final int g2 = (color2 >>> 8) & 0xFF;
        final int b2 = color2 & 0xFF;
        final int r = (int) (r1 + (r2 - r1) * t);
        final int g = (int) (g1 + (g2 - g1) * t);
        final int b = (int) (b1 + (b2 - b1) * t);
        return (r << 16) | (g << 8) | b;
    }

    private static void handleUrl(final String url) {
        // Pretty much copy-paste from GuiChat.
        try {
            final Class<?> desktop = Class.forName("java.awt.Desktop");
            final Object instance = desktop.getMethod("getDesktop").invoke(null);
            desktop.getMethod("browse", URI.class).invoke(instance, new URI(url));
        } catch (final Throwable t) {
            final ClientPlayerEntity player = Minecraft.getInstance().player;
            if (player != null) {
                player.displayClientMessage(new StringTextComponent(t.toString()), false);
            }
        }
    }

    @Override
    public String toString() {
        return String.format("[%s](%s)", super.toString(), url);
    }
}
