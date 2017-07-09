package li.cil.tis3d.client.renderer.block;

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

    protected final void drawFrame(final RenderBlocks renderer, final Runnable renderCall) {
        renderer.setRenderBounds(
                0 / 16f, 0 / 16f, 0 / 16f,
                1 / 16f, 1 / 16f, 16 / 16f);
        renderCall.run();
        renderer.setRenderBounds(
                15 / 16f, 0 / 16f, 0 / 16f,
                16 / 16f, 1 / 16f, 16 / 16f);
        renderCall.run();
        renderer.setRenderBounds(
                0 / 16f, 15 / 16f, 0 / 16f,
                1 / 16f, 16 / 16f, 16 / 16f);
        renderCall.run();
        renderer.setRenderBounds(
                15 / 16f, 15 / 16f, 0 / 16f,
                16 / 16f, 16 / 16f, 16 / 16f);
        renderCall.run();

        renderer.setRenderBounds(
                1 / 16f, 0 / 16f, 0 / 16f,
                15 / 16f, 1 / 16f, 1 / 16f);
        renderCall.run();
        renderer.setRenderBounds(
                1 / 16f, 0 / 16f, 15 / 16f,
                15 / 16f, 1 / 16f, 16 / 16f);
        renderCall.run();
        renderer.setRenderBounds(
                1 / 16f, 15 / 16f, 0 / 16f,
                15 / 16f, 16 / 16f, 1 / 16f);
        renderCall.run();
        renderer.setRenderBounds(
                1 / 16f, 15 / 16f, 15 / 16f,
                15 / 16f, 16 / 16f, 16 / 16f);
        renderCall.run();

        renderer.setRenderBounds(
                0 / 16f, 1 / 16f, 0 / 16f,
                1 / 16f, 15 / 16f, 1 / 16f);
        renderCall.run();
        renderer.setRenderBounds(
                15 / 16f, 1 / 16f, 0 / 16f,
                16 / 16f, 15 / 16f, 1 / 16f);
        renderCall.run();
        renderer.setRenderBounds(
                0 / 16f, 1 / 16f, 15 / 16f,
                1 / 16f, 15 / 16f, 16 / 16f);
        renderCall.run();
        renderer.setRenderBounds(
                15 / 16f, 1 / 16f, 15 / 16f,
                16 / 16f, 15 / 16f, 16 / 16f);
        renderCall.run();
    }

    protected final void drawRivets(final RenderBlocks renderer, final Runnable renderCall) {
        renderer.setRenderBounds(
                2 / 16f, 2 / 16f, 0.5f / 16f,
                3 / 16f, 3 / 16f, 15.5f / 16f);
        renderCall.run();
        renderer.setRenderBounds(
                13 / 16f, 2 / 16f, 0.5f / 16f,
                14 / 16f, 3 / 16f, 15.5f / 16f);
        renderCall.run();
        renderer.setRenderBounds(
                2 / 16f, 13 / 16f, 0.5f / 16f,
                3 / 16f, 14 / 16f, 15.5f / 16f);
        renderCall.run();
        renderer.setRenderBounds(
                13 / 16f, 13 / 16f, 0.5f / 16f,
                14 / 16f, 14 / 16f, 15.5f / 16f);
        renderCall.run();

        renderer.setRenderBounds(
                0.5f / 16f, 2 / 16f, 2 / 16f,
                15.5f / 16f, 3 / 16f, 3 / 16f);
        renderCall.run();
        renderer.setRenderBounds(
                0.5f / 16f, 13 / 16f, 2 / 16f,
                15.5f / 16f, 14 / 16f, 3 / 16f);
        renderCall.run();
        renderer.setRenderBounds(
                0.5f / 16f, 2 / 16f, 13 / 16f,
                15.5f / 16f, 3 / 16f, 14 / 16f);
        renderCall.run();
        renderer.setRenderBounds(
                0.5f / 16f, 13 / 16f, 13 / 16f,
                15.5f / 16f, 14 / 16f, 14 / 16f);
        renderCall.run();

        renderer.setRenderBounds(
                2 / 16f, 0.5f / 16f, 2 / 16f,
                3 / 16f, 15.5f / 16f, 3 / 16f);
        renderCall.run();
        renderer.setRenderBounds(
                13 / 16f, 0.5f / 16f, 2 / 16f,
                14 / 16f, 15.5f / 16f, 3 / 16f);
        renderCall.run();
        renderer.setRenderBounds(
                2 / 16f, 0.5f / 16f, 13 / 16f,
                3 / 16f, 15.5f / 16f, 14 / 16f);
        renderCall.run();
        renderer.setRenderBounds(
                13 / 16f, 0.5f / 16f, 13 / 16f,
                14 / 16f, 15.5f / 16f, 14 / 16f);
        renderCall.run();
    }
}
