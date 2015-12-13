package li.cil.tis3d.api.prefab.client;

import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

/**
 * Simple implementation of the {@link AbstractModuleItemRenderer} using the
 * texture specified for the item module as the overlay texture.
 *
 * @author Sangar, Vexatos
 */
public class SimpleModuleRenderer extends AbstractModuleItemRenderer {
    private boolean ignoreLighting;

    public SimpleModuleRenderer setIgnoreLighting(final boolean value) {
        this.ignoreLighting = value;
        return this;
    }

    // --------------------------------------------------------------------- //
    // AbstractModuleItemRenderer

    @Override
    public IIcon getOverlayIcon(final ItemRenderType type, final ItemStack item, final Object... data) {
        return item.getItem().getIconFromDamage(item.getItemDamage());
    }

    @Override
    protected boolean shouldIgnoreLighting(final ItemRenderType type, final ItemStack item, final Object... data) {
        return ignoreLighting;
    }
}
