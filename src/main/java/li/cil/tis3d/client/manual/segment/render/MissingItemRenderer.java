package li.cil.tis3d.client.manual.segment.render;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.manual.InteractiveImageRenderer;
import net.minecraft.util.ResourceLocation;

public final class MissingItemRenderer extends TextureImageRenderer implements InteractiveImageRenderer {
    private static final ResourceLocation LOCATION_MANUAL_MISSING = new ResourceLocation(API.MOD_ID, "textures/gui/manualMissing.png");

    private final String tooltip;

    public MissingItemRenderer(final String tooltip) {
        super(LOCATION_MANUAL_MISSING);
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
