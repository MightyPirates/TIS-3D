package li.cil.manual.api;

import li.cil.manual.api.util.Constants;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Style definition that may be used when using the built-in manual
 * screen and requiring the option for additional customizations.
 */
@OnlyIn(Dist.CLIENT)
public interface ManualScreenStyle {
    /**
     * Default implementation of a screen style.
     */
    ManualScreenStyle DEFAULT = new ManualScreenStyle() {
    };

    default ResourceLocation getWindowBackground() {
        return new ResourceLocation(Constants.MOD_ID, "textures/gui/manual.png");
    }

    default ResourceLocation getScrollButtonTexture() {
        return new ResourceLocation(Constants.MOD_ID, "textures/gui/scroll_button.png");
    }

    default ResourceLocation getTabButtonTexture() {
        return new ResourceLocation(Constants.MOD_ID, "textures/gui/tab_button.png");
    }

    default Rectangle2d getWindowRect() {
        return new Rectangle2d(0, 0, 256, 256);
    }

    default Rectangle2d getDocumentRect() {
        return new Rectangle2d(24, 16, 208, 216);
    }

    default Rectangle2d getScrollBarRect() {
        return new Rectangle2d(250, 16, 20, 216);
    }

    default Rectangle2d getScrollButtonRect() {
        return new Rectangle2d(0, 0, 20, 12);
    }

    default Rectangle2d getTabAreaRect() {
        return new Rectangle2d(-52, 25, 64, 216);
    }

    default Rectangle2d getTabRect() {
        return new Rectangle2d(0, 0, 64, 24);
    }

    default int getTabOverlap() {
        return 4;
    }

    default int getTabHoverShift() {
        return 20;
    }
}
