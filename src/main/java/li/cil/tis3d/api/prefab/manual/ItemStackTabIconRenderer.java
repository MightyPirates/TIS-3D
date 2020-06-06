package li.cil.tis3d.api.prefab.manual;

import com.mojang.blaze3d.platform.GlStateManager;
import li.cil.tis3d.api.manual.TabIconRenderer;
import li.cil.tis3d.api.util.RenderUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.item.ItemStack;

/**
 * Simple implementation of a tab icon renderer using an item stack as its graphic.
 */
@SuppressWarnings("UnusedDeclaration")
public class ItemStackTabIconRenderer implements TabIconRenderer {
    private final ItemStack stack;

    public ItemStackTabIconRenderer(final ItemStack stack) {
        this.stack = stack;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void render() {
        GlStateManager.enableRescaleNormal();
        RenderUtil.ignoreLighting();
        DiffuseLighting.enableForItems();
        MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(stack, 0, 0);
        DiffuseLighting.disable();
    }
}
