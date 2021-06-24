package li.cil.tis3d.api.prefab.manual;

import li.cil.tis3d.api.manual.InteractiveContentRenderer;
import net.minecraft.util.text.ITextComponent;

/**
 * Simple base implementation of {@link InteractiveContentRenderer}.
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class AbstractInteractiveContentRenderer implements InteractiveContentRenderer {
    @Override
    public ITextComponent getTooltip(final ITextComponent tooltip) {
        return tooltip;
    }

    @Override
    public boolean onMouseClick(final double mouseX, final double mouseY) {
        return false;
    }
}
