package li.cil.tis3d.common.module;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.traits.Redstone;
import li.cil.tis3d.api.prefab.module.AbstractModuleWithRotation;
import li.cil.tis3d.api.util.RenderContext;
import li.cil.tis3d.client.renderer.Textures;
import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class ModuleRedstone extends AbstractModuleWithRotation implements Redstone {
    // --------------------------------------------------------------------- //
    // Persisted data

    private short output = 0;
    private short input = 0;

    // --------------------------------------------------------------------- //
    // Computed data

    // NBT tag names.
    private static final String TAG_OUTPUT = "output";
    private static final String TAG_INPUT = "input";

    // Data packet types.
    private static final byte DATA_TYPE_UPDATE = 0;

    // Rendering info.
    private static final float OUTPUT_X = 9 / 32f;
    private static final float INPUT_X = 20 / 32f;
    private static final float SHARED_V0 = 10 / 32f;
    private static final float SHARED_Y = 25 / 32f;
    private static final float SHARED_W = 3 / 32f;
    private static final float SHARED_H = SHARED_Y - SHARED_V0;

    /**
     * The last tick we updated. Used to avoid changing output multiple times a
     * tick, which is usually pointless and really bad for performance.
     */
    private long lastStep = 0L;

    /**
     * Something changed last tick after the first neighbor block update, so
     * we need to update again in the next tick (if we don't anyway).
     */
    private boolean scheduledNeighborUpdate = false;

    // --------------------------------------------------------------------- //

    public ModuleRedstone(final Casing casing, final Face face) {
        super(casing, face);
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        final World world = getCasing().getCasingLevel();

        for (final Port port : Port.VALUES) {
            stepOutput(port);
            stepInput(port);
        }

        if (scheduledNeighborUpdate && world.getGameTime() > lastStep) {
            notifyNeighbors();
        }

        lastStep = world.getGameTime();
    }

    @Override
    public void onDisabled() {
        input = 0;
        output = 0;

        notifyNeighbors();

        sendData();
    }

    @Override
    public void onEnabled() {
        sendData();
    }

    @Override
    public void onWriteComplete(final Port port) {
        // Start writing again right away to write as fast as possible.
        stepOutput(port);
    }

    @Override
    public void onData(final ByteBuf data) {
        input = data.readShort();
        output = data.readShort();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(final RenderContext context) {
        final MatrixStack matrixStack = context.getMatrixStack();
        matrixStack.pushPose();
        rotateForRendering(matrixStack);

        // Draw base overlay.
        context.drawAtlasSpriteUnlit(Textures.LOCATION_OVERLAY_MODULE_REDSTONE);

        if (!getCasing().isEnabled()) {
            matrixStack.popPose();
            return;
        }

        // Draw output bar.
        final float relativeOutput = output / 15f;
        final float heightOutput = relativeOutput * SHARED_H;
        final float v0Output = SHARED_Y - heightOutput;
        context.drawAtlasSpriteUnlit(Textures.LOCATION_OVERLAY_MODULE_REDSTONE_BARS,
            OUTPUT_X, v0Output, SHARED_W, heightOutput);

        // Draw input bar.
        final float relativeInput = input / 15f;
        final float heightInput = relativeInput * SHARED_H;
        final float v0Input = SHARED_Y - heightInput;
        context.drawAtlasSpriteUnlit(Textures.LOCATION_OVERLAY_MODULE_REDSTONE_BARS,
            INPUT_X, v0Input, SHARED_W, heightInput);

        matrixStack.popPose();
    }

    @Override
    public void readFromNBT(final CompoundNBT nbt) {
        super.readFromNBT(nbt);

        output = (short) Math.max(0, Math.min(15, nbt.getShort(TAG_OUTPUT)));
        input = (short) Math.max(0, Math.min(15, nbt.getShort(TAG_INPUT)));
    }

    @Override
    public void writeToNBT(final CompoundNBT nbt) {
        super.writeToNBT(nbt);

        nbt.putInt(TAG_OUTPUT, output);
        nbt.putInt(TAG_INPUT, input);
    }

    // --------------------------------------------------------------------- //
    // Redstone

    @Override
    public short getRedstoneOutput() {
        return output;
    }

    @Override
    public void setRedstoneInput(final short value) {
        // We never call this on the client side, but other might...
        final World world = getCasing().getCasingLevel();
        if (world.isClientSide()) {
            return;
        }

        // Clamp to valid redstone range.
        final short validatedValue = (short) Math.max(0, Math.min(15, value));
        if (validatedValue == input) {
            return;
        }

        input = validatedValue;

        // If the value changed, make sure we're saved.
        getCasing().setChanged();

        // The value changed, cancel our output to make sure it's up-to-date.
        cancelWrite();

        // Update client representation.
        sendData();
    }

    // --------------------------------------------------------------------- //

    /**
     * Update the output of the module, pushing a value read from any pipe.
     */
    private void stepOutput(final Port port) {
        final Pipe sendingPipe = getCasing().getSendingPipe(getFace(), port);
        if (!sendingPipe.isWriting()) {
            sendingPipe.beginWrite(input);
        }
    }

    /**
     * Update the input of the module, pushing the current input to any pipe.
     */
    private void stepInput(final Port port) {
        // Continuously read from all ports, set output to last received value.
        final Pipe receivingPipe = getCasing().getReceivingPipe(getFace(), port);
        if (!receivingPipe.isReading()) {
            receivingPipe.beginRead();
        }
        if (receivingPipe.canTransfer()) {
            setRedstoneOutput(receivingPipe.read());
        }
    }

    /**
     * Update the redstone signal we're outputting.
     *
     * @param value the new output value.
     */
    private void setRedstoneOutput(final short value) {
        // Clamp to valid redstone range.
        final short validatedValue = (short) Math.max(0, Math.min(15, value));
        if (validatedValue == output) {
            return;
        }

        output = validatedValue;

        // If the value changed, make sure we're saved.
        getCasing().setChanged();

        // Notify neighbors, avoid multiple world updates per tick.
        scheduledNeighborUpdate = true;

        sendData();
    }

    /**
     * Notify all neighbors of a block update, to let them realize our output changed.
     */
    private void notifyNeighbors() {
        final World world = getCasing().getCasingLevel();

        scheduledNeighborUpdate = false;
        final Block blockType = world.getBlockState(getCasing().getPosition()).getBlock();
        world.updateNeighborsAt(getCasing().getPosition(), blockType);
    }

    /**
     * Send the current state of the module (to the client).
     */
    private void sendData() {
        final ByteBuf data = Unpooled.buffer();
        data.writeShort(input);
        data.writeShort(output);
        getCasing().sendData(getFace(), data, DATA_TYPE_UPDATE);
    }
}
