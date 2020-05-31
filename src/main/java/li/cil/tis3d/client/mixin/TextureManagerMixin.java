package li.cil.tis3d.client.mixin;

import java.util.Map;
import java.util.Set;
import li.cil.tis3d.client.ext.TextureManagerExt;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.texture.TextureTickListener;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;


@Mixin(TextureManager.class)
public abstract class TextureManagerMixin implements TextureManagerExt {
    @Shadow
    private Map<Identifier, AbstractTexture> textures;

    @Shadow
    private Set<TextureTickListener> tickListeners;

    public void unregisterTexture(final Identifier id) {
        final AbstractTexture texture = textures.get(id);

        if (texture instanceof TextureTickListener) {
            tickListeners.remove(texture);
        }

        textures.remove(id);
    }
}
