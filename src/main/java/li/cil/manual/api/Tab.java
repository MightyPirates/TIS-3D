package li.cil.manual.api;

import com.mojang.blaze3d.matrix.MatrixStack;
import li.cil.manual.api.util.ComparableRegistryEntry;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

/**
 * Represents a tab displayed next to the manual.
 * <p>
 * These are intended to link to index pages, and for the time being there
 * a relatively low number of tabs that can be displayed, so I'd ask you to
 * only register as many tabs as actually, technically <em>needed</em>. Which
 * will usually be one, for your main index page.
 */
@OnlyIn(Dist.CLIENT)
public interface Tab extends ComparableRegistryEntry<Tab> {
    /**
     * Called when icon of a tab should be rendered.
     * <p>
     * This should render something in a 16x16 area. The OpenGL state has been
     * adjusted so that drawing starts at (0,0,0), and should go to (16,16,0).
     *
     * @param matrixStack the current matrix stack.
     */
    void renderIcon(MatrixStack matrixStack);

    /**
     * The (ideally localized) tooltip for the tab.
     *
     * @param tooltip the list to add the tooltip to.
     */
    void getTooltip(List<ITextComponent> tooltip);

    /**
     * The path to the page to open when the tab is clicked.
     *
     * @return the linked page path.
     */
    String getPath();
}
