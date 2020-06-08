package li.cil.tis3d.api.prefab.manual;

import li.cil.tis3d.api.manual.InteractiveImageRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Simple base implementation of {@link li.cil.tis3d.api.manual.InteractiveImageRenderer}.
 */
@SuppressWarnings("UnusedDeclaration")
@Environment(EnvType.CLIENT)
public abstract class AbstractInteractiveImageRenderer implements InteractiveImageRenderer {
    @Override
    public String getTooltip(final String tooltip) {
        return tooltip;
    }

    @Override
    public boolean onMouseClick(final int mouseX, final int mouseY) {
        return false;
    }
}
