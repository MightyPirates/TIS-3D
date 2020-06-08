package li.cil.tis3d.client.ext;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public interface TextureManagerExt {
    static TextureManagerExt from(final TextureManager self) {
        return (TextureManagerExt)self;
    }

    /**
     * Remove all references to a texture previously registered
     * via {@link net.minecraft.client.texture.TextureManager#registerTexture}.
     *
     * @param id the identifier under which the texture was registered.
     */
    void unregisterTexture(final Identifier id);
}
