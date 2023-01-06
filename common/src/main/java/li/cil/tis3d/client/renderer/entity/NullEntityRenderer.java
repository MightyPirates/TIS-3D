package li.cil.tis3d.client.renderer.entity;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public final class NullEntityRenderer<T extends Entity> extends EntityRenderer<T> {
    public NullEntityRenderer(final EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(final T entity, final Frustum frustum, final double cameraX, final double cameraY, final double cameraZ) {
        return false;
    }

    @Override
    public ResourceLocation getTextureLocation(final T entity) {
        return MissingTextureAtlasSprite.getLocation();
    }
}
