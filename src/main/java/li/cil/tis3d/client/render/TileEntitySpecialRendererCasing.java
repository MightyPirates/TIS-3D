package li.cil.tis3d.client.render;

import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.common.tile.TileEntityCasing;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

/**
 * Tile entity renderer for casings, used to dynamically render stuff for
 * different modules (in particular to allow dynamic displayed content, but
 * also so as not to spam the model registry with potentially a gazillion
 * block states for static individual texturing).
 */
public class TileEntitySpecialRendererCasing extends TileEntitySpecialRenderer<TileEntityCasing> {
    @Override
    public void renderTileEntityAt(final TileEntityCasing casing, final double x, final double y, final double z, final float partialTicks, final int destroyStage) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);

        for (final Face face : Face.VALUES) {
            final Module module = casing.getModule(face);
            if (module == null) {
                continue;
            }

            GlStateManager.pushMatrix();

            switch (face) {
                case Y_NEG:
                    GlStateManager.rotate(-90, 1, 0, 0);
                    break;
                case Y_POS:
                    GlStateManager.rotate(90, 1, 0, 0);
                    break;
                case Z_NEG:
                    GlStateManager.rotate(0, 0, 1, 0);
                    break;
                case Z_POS:
                    GlStateManager.rotate(180, 0, 1, 0);
                    break;
                case X_NEG:
                    GlStateManager.rotate(90, 0, 1, 0);
                    break;
                case X_POS:
                    GlStateManager.rotate(-90, 0, 1, 0);
                    break;
            }

            GlStateManager.translate(0.5, 0.5, -0.505);
            GlStateManager.scale(-1, -1, 1);

            module.render(partialTicks);

            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
    }
}
