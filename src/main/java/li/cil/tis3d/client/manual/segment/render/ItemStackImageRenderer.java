package li.cil.tis3d.client.manual.segment.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import li.cil.tis3d.api.manual.ImageRenderer;
import li.cil.tis3d.api.util.RenderUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
public final class ItemStackImageRenderer implements ImageRenderer {
    /**
     * How long to show individual stacks, in milliseconds, before switching to the next.
     */
    private static final int CYCLE_SPEED = 1000;

    private final ItemStack[] stacks;

    public ItemStackImageRenderer(final ItemStack... stacks) {
        this.stacks = stacks;
    }

    @Override
    public int getWidth() {
        return 32;
    }

    @Override
    public int getHeight() {
        return 32;
    }

    @Override
    public void render(final MatrixStack matrices, final int mouseX, final int mouseY) {
        final MinecraftClient mc = MinecraftClient.getInstance();
        final int index = (int)(System.currentTimeMillis() % (CYCLE_SPEED * stacks.length)) / CYCLE_SPEED;
        final ItemStack stack = stacks[index];

        RenderUtil.ignoreLighting();
        DiffuseLighting.enableGuiDepthLighting();
        mc.getItemRenderer().renderGuiItemIcon(stack, mouseX, mouseY);
        DiffuseLighting.disableGuiDepthLighting();
    }
}
