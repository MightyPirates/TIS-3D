package li.cil.manual.api.util;

import li.cil.manual.api.ManualModel;
import li.cil.manual.api.ManualScreenStyle;
import li.cil.manual.api.ManualStyle;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * May be fired to request showing the default implementation of the manual screen for the
 * given manual and style.
 */
@OnlyIn(Dist.CLIENT)
public final class ShowManualScreenEvent extends Event {
    private final ManualModel manualModel;
    @Nullable private final ManualStyle manualStyle;
    @Nullable private final ManualScreenStyle screenStyle;

    public ShowManualScreenEvent(final ManualModel model, @Nullable final ManualStyle manualStyle, @Nullable final ManualScreenStyle screenStyle) {
        this.manualModel = model;
        this.manualStyle = manualStyle;
        this.screenStyle = screenStyle;
    }

    public ShowManualScreenEvent(final ManualModel model) {
        this(model, null, null);
    }

    /**
     * The manual to show the default manual screen for.
     * <p>
     * This defines the contents of the displayed manual.
     *
     * @return the manual.
     */
    public ManualModel getManualModel() {
        return manualModel;
    }

    /**
     * The style to use for the default manual screen.
     * <p>
     * This defines the looks of the displayed manual.
     *
     * @return the style.
     */
    public Optional<ManualStyle> getManualStyle() {
        return Optional.ofNullable(manualStyle);
    }

    /**
     * The style overrides for the manual screen.
     *
     * @return the screen style.
     */
    public Optional<ManualScreenStyle> getScreenStyle() {
        return Optional.ofNullable(screenStyle);
    }
}
