package li.cil.tis3d.system.module;

import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.Pipe;
import li.cil.tis3d.api.Port;
import li.cil.tis3d.api.prefab.AbstractModule;
import li.cil.tis3d.client.TextureLoader;
import li.cil.tis3d.system.module.execution.MachineState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;

public final class ModuleRandom extends AbstractModule {
    public ModuleRandom(final Casing casing, final Face face) {
        super(casing, face);
    }

    private void stepOutput(final Port port) {
        // For reading redstone values, wait for readers to provide up-to-date
        // values, instead of an old value from when we started writing.
        final Pipe sendingPipe = getCasing().getSendingPipe(getFace(), port);
        if (sendingPipe.isReading()) {
            if (!sendingPipe.isWriting()) {
                sendingPipe.beginWrite(getCasing().getWorld().rand.nextInt(MachineState.MAX_VALUE * 2 + 1) - MachineState.MAX_VALUE);
            }
        }
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        for (final Port port : Port.VALUES) {
            stepOutput(port);
        }
    }

    @Override
    public void onWriteComplete(final Port port) {
        // Start writing again right away to write as fast as possible.
        stepOutput(port);
    }

    @Override
    public void render(final boolean enabled, final float partialTicks) {
        if (!enabled) {
            return;
        }

        RenderHelper.disableStandardItemLighting();
        GlStateManager.enableBlend();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240 / 1.0F, 0 / 1.0F);

        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        final TextureAtlasSprite icon = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(TextureLoader.LOCATION_MODULE_RANDOM_OVERLAY.toString());
        drawQuad(icon.getMinU(), icon.getMinV(), icon.getMaxU(), icon.getMaxV());

        GlStateManager.disableBlend();
        RenderHelper.enableStandardItemLighting();
    }
}
