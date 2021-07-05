package li.cil.tis3d.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class InvisibleEntityRenderer<T extends Entity> extends EntityRenderer<T> {
    public InvisibleEntityRenderer(final EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(final T entity) {
        return null;
    }

    @Override
    public boolean shouldRender(final T entity, final Frustum frustum, final double x, final double y, final double z) {
        return false;
    }
}
