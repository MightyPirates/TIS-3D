package li.cil.tis3d.common.module;

import com.mojang.blaze3d.vertex.PoseStack;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import li.cil.manual.api.render.FontRenderer;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.module.AbstractModuleWithRotation;
import li.cil.tis3d.api.util.RenderContext;
import li.cil.tis3d.client.renderer.Textures;
import li.cil.tis3d.util.Color;
import net.minecraft.nbt.CompoundTag;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * The timer module can be used to wait for a specific amount of game time.
 * It is configured by writing a value to any of its ports, and can be
 * waited on by reading from any of its port - which will only be written to
 * once the timer has expired (reached zero).
 * <p>
 * This module will receive data on all ports and push back a value while the
 * timer is zero.
 */
public final class TimerModule extends AbstractModuleWithRotation {
    // --------------------------------------------------------------------- //
    // Persisted data

    // The game time the timer elapses at.
    private long timer;

    // --------------------------------------------------------------------- //
    // Computed data

    // NBT data names.
    private static final String TAG_TIMER = "timer";

    // Data packet types.
    private static final byte DATA_TYPE_UPDATE = 0;

    // The value written to all ports once the timer has reached zero.
    private static final short OUTPUT_VALUE = 1;

    // Cached elapsed state.
    private boolean hasElapsed;

    // --------------------------------------------------------------------- //

    public TimerModule(final Casing casing, final Face face) {
        super(casing, face);
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        if (!hasElapsed) {
            final long gameTime = getCasing().getCasingLevel().getGameTime();
            if (gameTime >= timer) {
                hasElapsed = true;
            }
        }

        stepOutput();
        stepInput();
    }

    @Override
    public void onDisabled() {
        // Clear timer on shutdown.
        timer = 0L;
        hasElapsed = true;

        sendData();
    }

    @Override
    public void onWriteComplete(final Port port) {
        // Start writing again right away to write as fast as possible.
        stepOutput();
    }

    @Override
    public void onData(final ByteBuf data) {
        timer = data.readLong();
        hasElapsed = false; // Recompute in render().
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void render(final RenderContext context) {
        if (!getCasing().isEnabled()) {
            return;
        }

        final PoseStack matrixStack = context.getMatrixStack();
        matrixStack.pushPose();
        rotateForRendering(matrixStack);

        context.drawAtlasQuadUnlit(Textures.LOCATION_OVERLAY_MODULE_TIMER);

        // Render detailed state when player is close.
        if (!hasElapsed && context.closeEnoughForDetails(getCasing().getPosition())) {
            final long gameTime = context.getDispatcher().level.getGameTime();
            final float remaining = (float) (timer - gameTime) - context.getPartialTicks();
            if (remaining <= 0) {
                hasElapsed = true;
            } else {
                drawState(context, remaining);
            }
        }

        matrixStack.popPose();
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);

        timer = tag.getLong(TAG_TIMER);
    }

    @Override
    public void save(final CompoundTag tag) {
        super.save(tag);

        tag.putLong(TAG_TIMER, timer);
    }

    // --------------------------------------------------------------------- //

    /**
     * Set the timer to the specified value.
     *
     * @param value the value to set the timer to.
     */
    private void setTimer(final short value) {
        final long gameTime = getCasing().getCasingLevel().getGameTime();
        timer = gameTime + (value & 0xFFFF);
        hasElapsed = timer == gameTime;

        if (!hasElapsed) {
            cancelWrite();
        }

        sendData();
    }

    /**
     * Update the outputs of the timer, pushing a value if it has elapsed.
     */
    private void stepOutput() {
        // Don't write if the timer is still running.
        if (!hasElapsed) {
            return;
        }

        for (final Port port : Port.VALUES) {
            final Pipe sendingPipe = getCasing().getSendingPipe(getFace(), port);
            if (!sendingPipe.isWriting()) {
                sendingPipe.beginWrite(OUTPUT_VALUE);
            }
        }
    }

    /**
     * Update the inputs of the timer, setting the new timer value to read values.
     */
    private void stepInput() {
        for (final Port port : Port.VALUES) {
            // Continuously read from all ports, set timer to last received value.
            final Pipe receivingPipe = getCasing().getReceivingPipe(getFace(), port);
            if (!receivingPipe.isReading()) {
                receivingPipe.beginRead();
            }
            if (receivingPipe.canTransfer()) {
                // Set the value.
                setTimer(receivingPipe.read());
            }
        }
    }

    private void sendData() {
        final ByteBuf data = Unpooled.buffer();
        data.writeLong(timer);
        getCasing().sendData(getFace(), data, DATA_TYPE_UPDATE);
    }

    @Environment(EnvType.CLIENT)
    private void drawState(final RenderContext context, final float remaining) {
        final float milliseconds = remaining * 50f; // One tick is 50ms.
        final float seconds = milliseconds / 1000f;
        final int minutes = (int) (seconds / 60f);

        final String time;
        if (minutes > 0) {
            time = String.format("%d:%02d", minutes, (int) seconds % 60);
        } else {
            time = String.format("%.2f", seconds);
        }

        final FontRenderer fontRenderer = API.normalFontRenderer;

        final int width = fontRenderer.width(time);
        final int height = fontRenderer.lineHeight();

        final PoseStack matrixStack = context.getMatrixStack();
        matrixStack.translate(0.5f, 0.5f, 0);
        matrixStack.scale(1 / 80f, 1 / 80f, 1);
        matrixStack.translate(-width / 2f + 1, -height / 2f + 1, 0);

        context.drawString(fontRenderer, time, Color.WHITE);
    }
}
