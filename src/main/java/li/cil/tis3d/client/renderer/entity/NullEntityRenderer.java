package li.cil.tis3d.client.renderer.entity;

import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public final class NullEntityRenderer<T extends Entity> extends EntityRenderer<T> {
    public NullEntityRenderer(final EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public boolean shouldRender(final T entity, final ClippingHelper frustum, final double cameraX, final double cameraY, final double cameraZ) {
        return false;
    }

    @Override
    public ResourceLocation getTextureLocation(final T entity) {
        return MissingTextureSprite.getLocation();
    }
}
