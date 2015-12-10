package li.cil.tis3d.api.prefab;

import li.cil.tis3d.api.manual.InteractiveImageRenderer;

/**
 * Simple base implementation of {@link li.cil.tis3d.api.manual.InteractiveImageRenderer}.
 */
@SuppressWarnings("UnusedDeclaration")
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
