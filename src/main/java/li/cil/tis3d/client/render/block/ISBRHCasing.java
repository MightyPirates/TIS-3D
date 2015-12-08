package li.cil.tis3d.client.render.block;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.client.render.TextureLoader;
import li.cil.tis3d.common.TIS3D;
import li.cil.tis3d.common.tile.TileEntityCasing;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;

public class ISBRHCasing extends AbstractISBRH {
    public ISBRHCasing() {
        TIS3D.proxy.setCasingRenderId(getRenderId());
    }

    @Override
    public boolean renderWorldBlock(final IBlockAccess world, final int x, final int y, final int z, final Block block, final int modelId, final RenderBlocks renderer) {
        renderer.renderAllFaces = true;
        renderBlock(renderer, () -> renderer.renderStandardBlock(block, x, y, z));

        final TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof TileEntityCasing) {
            final TileEntityCasing casing = (TileEntityCasing) tileEntity;
            renderer.overrideBlockTexture = TextureLoader.ICON_CASING_MODULE;
            for (final Face face : Face.VALUES) {
                if (casing.getModule(face) != null) {
                    final float min = 1 / 16f;
                    final float max = 15 / 16f;
                    final float offsetMin = 14 / 16f;
                    final float offsetMax = 1 / 16f;
                    final float offsetX = Face.toEnumFacing(face).getFrontOffsetX();
                    final float offsetY = Face.toEnumFacing(face).getFrontOffsetY();
                    final float offsetZ = Face.toEnumFacing(face).getFrontOffsetZ();

                    final float x0, y0, z0, x1, y1, z1;
                    if (offsetX > 0) {
                        x0 = min + offsetMin * offsetX;
                        x1 = max + offsetMax * offsetX;
                    } else {
                        x0 = min + offsetMax * offsetX;
                        x1 = max + offsetMin * offsetX;
                    }
                    if (offsetY > 0) {
                        y0 = min + offsetMin * offsetY;
                        y1 = max + offsetMax * offsetY;
                    } else {
                        y0 = min + offsetMax * offsetY;
                        y1 = max + offsetMin * offsetY;
                    }
                    if (offsetZ > 0) {
                        z0 = min + offsetMin * offsetZ;
                        z1 = max + offsetMax * offsetZ;
                    } else {
                        z0 = min + offsetMax * offsetZ;
                        z1 = max + offsetMin * offsetZ;
                    }

                    renderer.setRenderBounds(x0, y0, z0, x1, y1, z1);
                    renderer.renderStandardBlock(block, x, y, z);
                }
            }
            renderer.clearOverrideBlockTexture();
        }

        return true;
    }

    @Override
    protected void renderBlock(final RenderBlocks renderer, final Runnable renderCall) {
        // Frame.
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

        // Block.
        renderer.setRenderBounds(
                1 / 16f, 1 / 16f, 1 / 16f,
                15 / 16f, 4 / 16f, 4 / 16f);
        renderCall.run();
        renderer.setRenderBounds(
                1 / 16f, 12 / 16f, 1 / 16f,
                15 / 16f, 15 / 16f, 4 / 16f);
        renderCall.run();
        renderer.setRenderBounds(
                1 / 16f, 1 / 16f, 12 / 16f,
                15 / 16f, 4 / 16f, 15 / 16f);
        renderCall.run();
        renderer.setRenderBounds(
                1 / 16f, 12 / 16f, 12 / 16f,
                15 / 16f, 15 / 16f, 15 / 16f);
        renderCall.run();

        renderer.setRenderBounds(
                1 / 16f, 1 / 16f, 4 / 16f,
                4 / 16f, 4 / 16f, 12 / 16f);
        renderCall.run();
        renderer.setRenderBounds(
                1 / 16f, 12 / 16f, 4 / 16f,
                4 / 16f, 15 / 16f, 12 / 16f);
        renderCall.run();
        renderer.setRenderBounds(
                12 / 16f, 1 / 16f, 4 / 16f,
                15 / 16f, 4 / 16f, 12 / 16f);
        renderCall.run();
        renderer.setRenderBounds(
                12 / 16f, 12 / 16f, 4 / 16f,
                15 / 16f, 15 / 16f, 12 / 16f);
        renderCall.run();

        renderer.setRenderBounds(
                1 / 16f, 4 / 16f, 1 / 16f,
                4 / 16f, 12 / 16f, 4 / 16f);
        renderCall.run();
        renderer.setRenderBounds(
                1 / 16f, 4 / 16f, 12 / 16f,
                4 / 16f, 12 / 16f, 15 / 16f);
        renderCall.run();
        renderer.setRenderBounds(
                12 / 16f, 4 / 16f, 1 / 16f,
                15 / 16f, 12 / 16f, 4 / 16f);
        renderCall.run();
        renderer.setRenderBounds(
                12 / 16f, 4 / 16f, 12 / 16f,
                15 / 16f, 12 / 16f, 15 / 16f);
        renderCall.run();

        renderer.setRenderBounds(
                2 / 16f, 2 / 16f, 2 / 16f,
                14 / 16f, 14 / 16f, 14 / 16f);
        renderCall.run();
    }
}
