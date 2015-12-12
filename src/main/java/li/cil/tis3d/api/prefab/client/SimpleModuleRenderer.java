package li.cil.tis3d.api.prefab.client;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

/**
 * @author Vexatos
 */
public class SimpleModuleRenderer extends ModuleItemRenderer {

	@Override
	public IIcon getFrontIcon(ItemRenderType type, ItemStack item, Object[] data) {
		return item.getItem().getIconFromDamage(item.getItemDamage());
	}

	@Override
	protected ResourceLocation getTextureLocation(ItemRenderType type, ItemStack item, Object[] data) {
		return TextureMap.locationItemsTexture;
	}
}
