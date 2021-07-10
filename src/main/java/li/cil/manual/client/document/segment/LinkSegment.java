package li.cil.manual.client.document.segment;

import li.cil.manual.api.ManualModel;
import li.cil.manual.api.ManualStyle;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public final class LinkSegment extends TextSegment implements InteractiveSegment {
    private static final int FADE_TIME = 500;

    // --------------------------------------------------------------------- //

    private final String url;
    private final boolean isWebUrl;
    private final boolean isLinkValid;
    private boolean isHovered;
    private long lastHovered = System.currentTimeMillis() - FADE_TIME;

    // --------------------------------------------------------------------- //

    public LinkSegment(final ManualModel manual, final ManualStyle style, final Segment parent, final String text, final String url) {
        super(manual, style, parent, text);
        this.url = url;
        this.isWebUrl = url.startsWith("http://") || url.startsWith("https://");
        this.isLinkValid = isWebUrl || manual.contentFor(manual.resolve(url)).isPresent();
    }

    // --------------------------------------------------------------------- //

    @Override
    public Optional<ITextComponent> getTooltip() {
        if (style.showLinkTooltip()) {
            return Optional.of(new StringTextComponent(url));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean mouseClicked() {
        if (isWebUrl) {
            Util.getPlatform().openUri(url);
        } else {
            manual.push(manual.resolve(url));
        }
        return true;
    }

    @Override
    public void setMouseHovered(final boolean value) {
        isHovered = value;
        if (!value) {
            lastHovered = System.currentTimeMillis();
        }
    }

    @Override
    public String toString() {
        return String.format("[%s](%s)", super.toString(), url);
    }

    // --------------------------------------------------------------------- //

    @Override
    protected int getColor() {
        final int color, hoverColor;
        if (isLinkValid) {
            color = style.getRegularLinkColor();
            hoverColor = style.getHoveredLinkColor();
        } else {
            color = style.getRegularDeadLinkColor();
            hoverColor = style.getHoveredDeadLinkColor();
        }

        final int timeSinceHover = isHovered ? 0 : (int) (System.currentTimeMillis() - lastHovered);
        if (timeSinceHover <= FADE_TIME) {
            return fadeColor(hoverColor, color, timeSinceHover / (float) FADE_TIME);
        } else {
            return color;
        }
    }

    @Override
    protected String getFormat() {
        if (isHovered) {
            return super.getFormat() + TextFormatting.UNDERLINE;
        } else {
            return super.getFormat();
        }
    }

    // --------------------------------------------------------------------- //

    private static int fadeColor(final int color1, final int color2, final float t) {
        final int a1 = (color1 >>> 24) & 0xFF;
        final int r1 = (color1 >>> 16) & 0xFF;
        final int g1 = (color1 >>> 8) & 0xFF;
        final int b1 = color1 & 0xFF;
        final int a2 = (color2 >>> 24) & 0xFF;
        final int r2 = (color2 >>> 16) & 0xFF;
        final int g2 = (color2 >>> 8) & 0xFF;
        final int b2 = color2 & 0xFF;
        final int a = (int) (a1 + (a2 - a1) * t);
        final int r = (int) (r1 + (r2 - r1) * t);
        final int g = (int) (g1 + (g2 - g1) * t);
        final int b = (int) (b1 + (b2 - b1) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
