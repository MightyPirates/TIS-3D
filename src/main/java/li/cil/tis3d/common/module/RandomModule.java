package li.cil.tis3d.common.module;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.module.AbstractModule;
import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.client.init.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.world.World;

import java.util.Random;

public final class RandomModule extends AbstractModule {
    public RandomModule(final Casing casing, final Face face) {
        super(casing, face);
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
        // No need to clear other writing pipes because we're outputting random
        // values anyway, so yey.

        // Start writing again right away to write as fast as possible.
        stepOutput(port);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void render(final BlockEntityRenderDispatcher rendererDispatcher, final float partialTicks,
                       final MatrixStack matrices, final VertexConsumerProvider vcp,
                       final int light, final int overlay) {
        if (!getCasing().isEnabled()) {
            return;
        }

        final Sprite sprite = RenderUtil.getSprite(Textures.LOCATION_OVERLAY_MODULE_RANDOM);
        final VertexConsumer vc = vcp.getBuffer(RenderLayer.getTranslucentNoCrumbling());

        RenderUtil.drawQuad(sprite, matrices.peek(), vc, RenderUtil.maxLight, overlay);
    }

    // --------------------------------------------------------------------- //

    /**
     * Update our outputs, pushing random values to the specified port.
     *
     * @param port the port to push to.
     */
    private void stepOutput(final Port port) {
        final Pipe sendingPipe = getCasing().getSendingPipe(getFace(), port);
        if (!sendingPipe.isWriting()) {
            final World world = getCasing().getCasingWorld();
            final Random random = world.random;
            final short value = (short)random.nextInt(0xFFFF + 1);
            sendingPipe.beginWrite(value);
        }
    }
}
