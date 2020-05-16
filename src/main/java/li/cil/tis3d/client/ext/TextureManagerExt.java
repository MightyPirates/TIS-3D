package li.cil.tis3d.client.ext;

import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;

public interface TextureManagerExt {
    public static TextureManagerExt from(final TextureManager self) {
        return (TextureManagerExt) self;
    }

    void unregisterTexture(final Identifier id);
}
