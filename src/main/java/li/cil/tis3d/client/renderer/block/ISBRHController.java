package li.cil.tis3d.client.renderer.block;

import li.cil.tis3d.common.TIS3D;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

public final class ISBRHController extends AbstractISBRH {
    public ISBRHController() {
        super();
        TIS3D.proxy.setControllerRenderId(getRenderId());
    }

    @Override
    public boolean renderWorldBlock(final IBlockAccess world, final int x, final int y, final int z, final Block block, final int modelId, final RenderBlocks renderer) {
        renderer.renderAllFaces = true;
        renderBlock(renderer, () -> renderer.renderStandardBlock(block, x, y, z));

        return true;
    }

    @Override
    protected void renderBlock(final RenderBlocks renderer, final Runnable renderCall) {
        drawFrame(renderer, renderCall);

        // Block.
        renderer.setRenderBounds(
                1 / 16f, 1 / 16f, 1 / 16f,
                15 / 16f, 15 / 16f, 15 / 16f);
        renderCall.run();

        // Faces.
        renderer.setRenderBounds(
                0 / 16f, 4 / 16f, 4 / 16f,
                1 / 16f, 12 / 16f, 12 / 16f);
        renderCall.run();
        renderer.setRenderBounds(
                15 / 16f, 4 / 16f, 4 / 16f,
                16 / 16f, 12 / 16f, 12 / 16f);
        renderCall.run();
        renderer.setRenderBounds(
                4 / 16f, 4 / 16f, 0 / 16f,
                12 / 16f, 12 / 16f, 1 / 16f);
        renderCall.run();
        renderer.setRenderBounds(
                4 / 16f, 4 / 16f, 15 / 16f,
                12 / 16f, 12 / 16f, 16 / 16f);
        renderCall.run();
        renderer.setRenderBounds(
                4 / 16f, 0 / 16f, 4 / 16f,
                12 / 16f, 1 / 16f, 12 / 16f);
        renderCall.run();
        renderer.setRenderBounds(
                4 / 16f, 15 / 16f, 4 / 16f,
                12 / 16f, 16 / 16f, 12 / 16f);
        renderCall.run();

        drawRivets(renderer, renderCall);
    }
}
