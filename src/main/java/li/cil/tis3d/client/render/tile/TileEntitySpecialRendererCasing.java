package li.cil.tis3d.client.render.tile;

import li.cil.tis3d.TIS3D;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.common.tile.TileEntityCasing;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

import java.util.HashSet;
import java.util.Set;

/**
 * Tile entity renderer for casings, used to dynamically render stuff for
 * different modules (in particular to allow dynamic displayed content, but
 * also so as not to spam the model registry with potentially a gazillion
 * block states for static individual texturing).
 */
public final class TileEntitySpecialRendererCasing extends TileEntitySpecialRenderer {
    private final static Set<Class<?>> BLACKLIST = new HashSet<>();

    @Override
    public void renderTileEntityAt(final TileEntity tileEntity, final double x, final double y, final double z, final float partialTicks) {
        final TileEntityCasing casing = (TileEntityCasing) tileEntity;

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

        // Render all modules, adjust GL state to allow easily rendering an
        // overlay in (0, 0, 0) to (1, 1, 0).
        for (final Face face : Face.VALUES) {
            final Module module = casing.getModule(face);
            if (module == null) {
                continue;
            }
            if (BLACKLIST.contains(module.getClass())) {
                continue;
            }

            GL11.glPushMatrix();

            switch (face) {
                case Y_NEG:
                    GL11.glRotatef(-90, 1, 0, 0);
                    break;
                case Y_POS:
                    GL11.glRotatef(90, 1, 0, 0);
                    break;
                case Z_NEG:
                    GL11.glRotatef(0, 0, 1, 0);
                    break;
                case Z_POS:
                    GL11.glRotatef(180, 0, 1, 0);
                    break;
                case X_NEG:
                    GL11.glRotatef(90, 0, 1, 0);
                    break;
                case X_POS:
                    GL11.glRotatef(-90, 0, 1, 0);
                    break;
            }

            GL11.glTranslatef(0.5f, 0.5f, -0.505f);
            GL11.glScalef(-1, -1, 1);

            try {
                module.render(casing.isEnabled(), partialTicks);
            } catch (final Exception e) {
                BLACKLIST.add(module.getClass());
                TIS3D.getLog().error("A module threw an exception while rendering, won't render again!", e);
            }

            GL11.glPopMatrix();
        }

        GL11.glPopMatrix();
    }
}
