package li.cil.tis3d.client.render.entity;

import net.minecraft.entity.Entity;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.Frustum;

public class InvisibleEntityRenderer<T extends Entity> extends EntityRenderer<T> {
    public InvisibleEntityRenderer(EntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public Identifier getTexture(T entity) {
        return null;
    }

    @Override
    public boolean shouldRender(T entity, Frustum frustum, double x, double y, double z) {
        return false;
    }
}
