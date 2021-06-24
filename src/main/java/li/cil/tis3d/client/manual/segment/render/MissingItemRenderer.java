package li.cil.tis3d.client.manual.segment.render;

import li.cil.tis3d.api.manual.InteractiveImageRenderer;
import li.cil.tis3d.client.renderer.Textures;
import net.minecraft.util.text.ITextComponent;

public final class MissingItemRenderer extends TextureImageRenderer implements InteractiveImageRenderer {
    private final ITextComponent tooltip;

    public MissingItemRenderer(final ITextComponent tooltip) {
        super(Textures.LOCATION_GUI_MANUAL_MISSING);
        this.tooltip = tooltip;
    }

    @Override
    public ITextComponent getTooltip(final ITextComponent tooltip) {
        return this.tooltip;
    }

    @Override
    public boolean onMouseClick(final double mouseX, final double mouseY) {
        return false;
    }
}
