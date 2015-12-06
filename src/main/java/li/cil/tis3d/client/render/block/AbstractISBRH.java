package li.cil.tis3d.client.render.block;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;

abstract class AbstractISBRH implements ISimpleBlockRenderingHandler {
    private final int renderId;

    protected AbstractISBRH() {
        renderId = RenderingRegistry.getNextAvailableRenderId();
    }

    @Override
    public boolean shouldRender3DInInventory(final int modelId) {
        return true;
    }

    @Override
    public int getRenderId() {
        return renderId;
    }

    @Override
    public void renderInventoryBlock(final Block block, final int metadata, final int modelId, final RenderBlocks renderer) {
        GL11.glPushMatrix();
        GL11.glTranslatef(-0.5f, -0.5f, -0.5f);
        Tessellator.instance.startDrawingQuads();
        renderBlock(renderer, () -> BlockRenderer.renderAllFaces(block, metadata, renderer));
        Tessellator.instance.draw();
        GL11.glPopMatrix();
    }

    protected abstract void renderBlock(final RenderBlocks renderer, final Runnable renderCall);
}
