package li.cil.tis3d.client.manual.segment.render;

import li.cil.tis3d.api.manual.ImageRenderer;
import li.cil.tis3d.api.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

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
    public void render(final int mouseX, final int mouseY) {
        final Minecraft mc = Minecraft.getMinecraft();
        final int index = (int) (System.currentTimeMillis() % (CYCLE_SPEED * stacks.length)) / CYCLE_SPEED;
        final ItemStack stack = stacks[index];

        GL11.glScalef(getWidth() / 16f, getHeight() / 16f, getWidth() / 16f);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderUtil.ignoreLighting();
        RenderHelper.enableGUIStandardItemLighting();
        RenderItem.getInstance().renderItemAndEffectIntoGUI(mc.fontRenderer, mc.getTextureManager(), stack, 0, 0);
        RenderHelper.disableStandardItemLighting();
    }
}
