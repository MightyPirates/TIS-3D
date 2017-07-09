package li.cil.tis3d.client.manual.segment.render;

import li.cil.tis3d.api.manual.InteractiveImageRenderer;
import li.cil.tis3d.client.renderer.TextureLoader;

public final class MissingItemRenderer extends TextureImageRenderer implements InteractiveImageRenderer {
    private final String tooltip;

    public MissingItemRenderer(final String tooltip) {
        super(TextureLoader.LOCATION_MANUAL_MISSING);
        this.tooltip = tooltip;
    }

    @Override
    public String getTooltip(final String tooltip) {
        return this.tooltip;
    }

    @Override
    public boolean onMouseClick(final int mouseX, final int mouseY) {
        return false;
    }
}
