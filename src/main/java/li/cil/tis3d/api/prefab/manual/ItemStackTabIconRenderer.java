package li.cil.tis3d.api.prefab.manual;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import li.cil.tis3d.api.manual.TabIconRenderer;
import li.cil.tis3d.api.util.RenderUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

/**
 * Simple implementation of a tab icon renderer using an item stack as its graphic.
 */
@SuppressWarnings("UnusedDeclaration")
@Environment(EnvType.CLIENT)
public class ItemStackTabIconRenderer implements TabIconRenderer {
    private final ItemStack stack;

    public ItemStackTabIconRenderer(final ItemStack stack) {
        this.stack = stack;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void render() {
        RenderSystem.enableColorLogicOp();
        //GlStateManager.enableRescaleNormal();
        RenderUtil.ignoreLighting();
        DiffuseLighting.enableGuiDepthLighting();
        MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(stack, 0, 0);
        DiffuseLighting.disableGuiDepthLighting();
    }
}
