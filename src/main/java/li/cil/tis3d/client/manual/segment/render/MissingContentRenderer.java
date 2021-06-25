package li.cil.tis3d.client.manual.segment.render;

import li.cil.tis3d.api.manual.InteractiveContentRenderer;
import li.cil.tis3d.client.renderer.Textures;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class MissingContentRenderer extends TextureContentRenderer implements InteractiveContentRenderer {
    private final ITextComponent tooltip;

    public MissingContentRenderer(final ITextComponent tooltip) {
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
