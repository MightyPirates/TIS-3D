package li.cil.manual.api.prefab.tab;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

/**
 * Simple implementation of a tab icon renderer using an item stack as its graphic.
 */
@OnlyIn(Dist.CLIENT)
public final class ItemStackTab extends AbstractTab {
    private final ItemStack stack;

    public ItemStackTab(final String path, @Nullable final ITextComponent tooltip, final ItemStack stack) {
        super(path, tooltip);
        this.stack = stack;
    }

    @Override
    public void renderIcon(final MatrixStack matrixStack) {
        // This is *nasty*, but sadly there's no renderItemAndEffectIntoGUI() variant that
        // takes a MatrixStack. Yet.

        final Vector4f position = new Vector4f(0, 0, 0, 1);
        position.transform(matrixStack.last().pose());

        RenderSystem.pushMatrix();
        RenderSystem.translated(position.x(), position.y(), 0);

        Minecraft.getInstance().getItemRenderer().renderGuiItem(stack, 0, 0);

        RenderSystem.popMatrix();

        // Unfuck GL state.
        RenderSystem.enableBlend();
    }
}
