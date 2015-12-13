package li.cil.tis3d.api.prefab.client;

import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

/**
 * @author Vexatos
 */
public class SimpleModuleRenderer extends AbstractModuleItemRenderer {

	@Override
	public IIcon getFrontIcon(ItemRenderType type, ItemStack item, Object[] data) {
		return item.getItem().getIconFromDamage(item.getItemDamage());
	}
}
