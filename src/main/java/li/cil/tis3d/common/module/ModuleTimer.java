package li.cil.tis3d.common.module;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.module.AbstractModuleWithRotation;
import li.cil.tis3d.api.util.RenderContext;
import li.cil.tis3d.client.renderer.Textures;
import li.cil.tis3d.client.renderer.font.FontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * The timer module can be used to wait for a specific amount of game time.
 * It is configured by writing a value to any of its ports, and can be
 * waited on by reading from any of its port - which will only be written to
 * once the timer has expired (reached zero).
 * <p>
 * This module will receive data on all ports and push back a value while the
 * timer is zero.
 */
public final class ModuleTimer extends AbstractModuleWithRotation {
    // --------------------------------------------------------------------- //
    // Persisted data

    // The world time the timer elapses at.
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

    public ModuleTimer(final Casing casing, final Face face) {
        super(casing, face);
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        if (!hasElapsed) {
            final long worldTime = getCasing().getCasingWorld().getGameTime();
            if (worldTime >= timer) {
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

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(final RenderContext context) {
        if (!getCasing().isEnabled()) {
            return;
        }

        final MatrixStack matrixStack = context.getMatrixStack();
        matrixStack.push();
        rotateForRendering(matrixStack);

        context.drawAtlasSpriteUnlit(Textures.LOCATION_OVERLAY_MODULE_TIMER);

        // Render detailed state when player is close.
        if (!hasElapsed && context.isWithinDetailRange(getCasing().getPosition())) {
            final Minecraft mc = Minecraft.getInstance();
            final long worldTime = mc.world != null ? mc.world.getGameTime() : 0;
            final float remaining = (float) (timer - worldTime) - context.getPartialTicks();
            if (remaining <= 0) {
                hasElapsed = true;
            } else {
                drawState(context, remaining);
            }
        }

        matrixStack.pop();
    }

    @Override
    public void readFromNBT(final CompoundNBT nbt) {
        super.readFromNBT(nbt);

        timer = nbt.getLong(TAG_TIMER);
    }

    @Override
    public void writeToNBT(final CompoundNBT nbt) {
        super.writeToNBT(nbt);

        nbt.putLong(TAG_TIMER, timer);
    }

    // --------------------------------------------------------------------- //

    /**
     * Set the timer to the specified value.
     *
     * @param value the value to set the timer to.
     */
    private void setTimer(final short value) {
        final long worldTime = getCasing().getCasingWorld().getGameTime();
        timer = worldTime + (value & 0xFFFF);
        hasElapsed = timer == worldTime;

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

    @OnlyIn(Dist.CLIENT)
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

        final int width = time.length() * fontRenderer.getCharWidth();
        final int height = fontRenderer.getCharHeight();

        final MatrixStack matrixStack = context.getMatrixStack();
        matrixStack.translate(0.5f, 0.5f, 0);
        matrixStack.scale(1 / 80f, 1 / 80f, 1);
        matrixStack.translate(-width / 2f + 1, -height / 2f + 1, 0);

        fontRenderer.drawString(matrixStack, context.bufferFactory, time);
    }
}
