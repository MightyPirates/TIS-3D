package li.cil.tis3d.client.manual.segment;

import li.cil.tis3d.api.ManualAPI;
import li.cil.tis3d.common.api.ManualAPIImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;
import java.net.URI;
import java.util.Optional;

@Environment(EnvType.CLIENT)
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

    public LinkSegment(final Segment parent, final String text, final String url) {
        super(parent, text);
        this.url = url;
    }

    private boolean isLinkValid() {
        if (!isLinkValidInitialized) {
            isLinkValid = (url.startsWith("http://") || url.startsWith("https://")) ||
                ManualAPI.contentFor(ManualAPIImpl.makeRelative(url, ManualAPIImpl.peekPath())) != null;
            isLinkValidInitialized = true;
        }
        return isLinkValid;
    }

    @Override
    protected Optional<Integer> color() {
        final int color, hoverColor;
        if (isLinkValid()) {
            color = NORMAL_COLOR;
            hoverColor = NORMAL_COLOR_HOVER;
        } else {
            color = ERROR_COLOR;
            hoverColor = ERROR_COLOR_HOVER;
        }

        final int timeSinceHover = (int)(System.currentTimeMillis() - lastHovered);
        if (timeSinceHover > FADE_TIME) {
            return Optional.of(color);
        } else {
            return Optional.of(fadeColor(hoverColor, color, timeSinceHover / (float)FADE_TIME));
        }
    }

    @Override
    public Optional<String> tooltip() {
        return Optional.of(url);
    }

    @Override
    public boolean onMouseClick(final int mouseX, final int mouseY) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            handleUrl(url);
        } else {
            ManualAPI.navigate(ManualAPIImpl.makeRelative(url, ManualAPIImpl.peekPath()));
        }
        return true;
    }

    @Override
    public void notifyHover() {
        lastHovered = System.currentTimeMillis();
    }

    private static int fadeColor(final int c1, final int c2, final float t) {
        final int r1 = (c1 >>> 16) & 0xFF;
        final int g1 = (c1 >>> 8) & 0xFF;
        final int b1 = c1 & 0xFF;
        final int r2 = (c2 >>> 16) & 0xFF;
        final int g2 = (c2 >>> 8) & 0xFF;
        final int b2 = c2 & 0xFF;
        final int r = (int)(r1 + (r2 - r1) * t);
        final int g = (int)(g1 + (g2 - g1) * t);
        final int b = (int)(b1 + (b2 - b1) * t);
        return (r << 16) | (g << 8) | b;
    }

    private static void handleUrl(final String url) {
        // Pretty much copy-paste from GuiChat.
        try {
            final Class<?> desktop = Class.forName("java.awt.Desktop");
            final Object instance = desktop.getMethod("getDesktop").invoke(null);
            desktop.getMethod("browse", URI.class).invoke(instance, new URI(url));
        } catch (final Throwable t) {
            MinecraftClient.getInstance().player.sendSystemMessage(new LiteralText(t.toString()), Util.NIL_UUID);
        }
    }

    @Override
    public String toString() {
        return String.format("[%s](%s)", text(), url);
    }
}
