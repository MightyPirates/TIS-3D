package li.cil.tis3d.client.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.util.RenderContext;
import li.cil.tis3d.common.module.DisplayModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

import java.util.Map;
import java.util.WeakHashMap;

@Environment(EnvType.CLIENT)
public final class DisplayModuleRenderer {
    private static final Map<DisplayModule, Entry> ENTRIES = new WeakHashMap<>();
    private static int nextTextureId;

    public static void render(final DisplayModule module, final RenderContext context) {
        final Entry entry = ENTRIES.computeIfAbsent(module, unused -> new Entry());
        entry.validate(module);
        context.drawQuad(context.getBuffer().getBuffer(entry.getOrCreateRenderLayer()),
            DisplayModule.MARGIN / 32f, DisplayModule.MARGIN / 32f,
            DisplayModule.RESOLUTION / 32f, DisplayModule.RESOLUTION / 32f);
    }

    public static void dispose(final DisplayModule module) {
        final Entry entry = ENTRIES.remove(module);
        if (entry != null) {
            entry.delete();
        }
    }

    private static final class Entry {
        private DynamicTexture texture;
        private Identifier textureId;
        private RenderType renderLayer;

        private DynamicTexture getOrCreateTexture() {
            if (texture == null) {
                texture = new DynamicTexture("tis3d/display_module", DisplayModule.RESOLUTION, DisplayModule.RESOLUTION, false);
            }
            return texture;
        }

        private RenderType getOrCreateRenderLayer() {
            if (renderLayer == null) {
                final DynamicTexture texture = getOrCreateTexture();
                textureId = Identifier.fromNamespaceAndPath(API.MOD_ID, "dynamic/display_module_" + (++nextTextureId));
                Minecraft.getInstance().getTextureManager().register(textureId, texture);
                renderLayer = ModRenderType.unlitTexture(textureId);
            }
            return renderLayer;
        }

        private void validate(final DisplayModule module) {
            if (!module.consumeImageDirty()) {
                return;
            }

            final DynamicTexture texture = getOrCreateTexture();
            final NativeImage nativeImage = texture.getPixels();
            if (nativeImage == null) {
                return;
            }

            final int[] image = module.getImage();
            int ip = 0;
            for (int iy = 0; iy < DisplayModule.RESOLUTION; iy++) {
                for (int ix = 0; ix < DisplayModule.RESOLUTION; ix++, ip++) {
                    final int argb = image[ip];
                    nativeImage.setPixelABGR(ix, iy, ARGB.color(
                        ARGB.alpha(argb), ARGB.blue(argb), ARGB.green(argb), ARGB.red(argb)));
                }
            }

            texture.upload();
        }

        private void delete() {
            final Minecraft minecraft = Minecraft.getInstance();
            if (textureId != null) {
                final Identifier id = textureId;
                minecraft.doRunTask(() -> minecraft.getTextureManager().release(id));
                textureId = null;
            }
            if (texture != null) {
                final DynamicTexture toClose = texture;
                minecraft.doRunTask(toClose::close);
                texture = null;
            }
            renderLayer = null;
        }
    }

    // --------------------------------------------------------------------- //

    private DisplayModuleRenderer() {
    }
}
