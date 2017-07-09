package li.cil.tis3d.client.renderer.block;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.common.util.ForgeDirection;

public final class BlockRenderer {
    public static void renderAllFaces(final Block block, final int metadata, final RenderBlocks renderer) {
        BlockRenderer.renderFaceYPos(block, metadata, renderer);
        BlockRenderer.renderFaceYNeg(block, metadata, renderer);
        BlockRenderer.renderFaceXPos(block, metadata, renderer);
        BlockRenderer.renderFaceXNeg(block, metadata, renderer);
        BlockRenderer.renderFaceZPos(block, metadata, renderer);
        BlockRenderer.renderFaceZNeg(block, metadata, renderer);
    }

    public static void renderFaceXPos(final Block block, final int metadata, final RenderBlocks renderer) {
        Tessellator.instance.setNormal(1, 0, 0);
        renderer.renderFaceXPos(block, 0, 0, 0, renderer.getBlockIconFromSideAndMetadata(block, ForgeDirection.EAST.ordinal(), metadata));
    }

    public static void renderFaceXNeg(final Block block, final int metadata, final RenderBlocks renderer) {
        Tessellator.instance.setNormal(-1, 0, 0);
        renderer.renderFaceXNeg(block, 0, 0, 0, renderer.getBlockIconFromSideAndMetadata(block, ForgeDirection.WEST.ordinal(), metadata));
    }

    public static void renderFaceYPos(final Block block, final int metadata, final RenderBlocks renderer) {
        Tessellator.instance.setNormal(0, 1, 0);
        renderer.renderFaceYPos(block, 0, 0, 0, renderer.getBlockIconFromSideAndMetadata(block, ForgeDirection.UP.ordinal(), metadata));
    }

    public static void renderFaceYNeg(final Block block, final int metadata, final RenderBlocks renderer) {
        Tessellator.instance.setNormal(0, -1, 0);
        renderer.renderFaceYNeg(block, 0, 0, 0, renderer.getBlockIconFromSideAndMetadata(block, ForgeDirection.DOWN.ordinal(), metadata));
    }

    public static void renderFaceZPos(final Block block, final int metadata, final RenderBlocks renderer) {
        Tessellator.instance.setNormal(0, 0, 1);
        renderer.renderFaceZPos(block, 0, 0, 0, renderer.getBlockIconFromSideAndMetadata(block, ForgeDirection.SOUTH.ordinal(), metadata));
    }

    public static void renderFaceZNeg(final Block block, final int metadata, final RenderBlocks renderer) {
        Tessellator.instance.setNormal(0, 0, -1);
        renderer.renderFaceZNeg(block, 0, 0, 0, renderer.getBlockIconFromSideAndMetadata(block, ForgeDirection.NORTH.ordinal(), metadata));
    }

    private BlockRenderer() {
    }
}
