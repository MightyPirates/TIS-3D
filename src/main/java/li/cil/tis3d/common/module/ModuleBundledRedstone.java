package li.cil.tis3d.common.module;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.traits.BundledRedstone;
import li.cil.tis3d.api.module.traits.BundledRedstoneOutputChangedEvent;
import li.cil.tis3d.api.prefab.module.AbstractModuleRotatable;
import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.util.ColorUtils;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;

public final class ModuleBundledRedstone extends AbstractModuleRotatable implements BundledRedstone {
    // --------------------------------------------------------------------- //
    // Persisted data

    private final short[] output = new short[16];
    private final short[] input = new short[16];
    private short channel = 0;

    // --------------------------------------------------------------------- //
    // Computed data

    // NBT tag names.
    private static final String TAG_OUTPUT = "output";
    private static final String TAG_INPUT = "input";
    private static final String TAG_CHANNEL = "channel";

    // Data packet types.
    private static final byte DATA_TYPE_UPDATE = 0;

    // Rendering info.
    private static final ResourceLocation LOCATION_OVERLAY = new ResourceLocation(API.MOD_ID, "textures/blocks/overlay/moduleBundledRedstone.png");
    private static final ResourceLocation LOCATION_COLORS_OVERLAY = new ResourceLocation(API.MOD_ID, "textures/blocks/overlay/moduleBundledRedstoneColors.png");
    private static final float V_STEP = 1 / 16f;

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

    public ModuleBundledRedstone(final Casing casing, final Face face) {
        super(casing, face);
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        assert (!getCasing().getCasingWorld().isRemote);

        for (final Port port : Port.VALUES) {
            stepOutput(port);
            stepInput(port);
        }

        if (scheduledNeighborUpdate && getCasing().getCasingWorld().getTotalWorldTime() > lastStep) {
            notifyNeighbors();
        }

        lastStep = getCasing().getCasingWorld().getTotalWorldTime();
    }

    @Override
    public void onDisabled() {
        assert (!getCasing().getCasingWorld().isRemote);

        Arrays.fill(input, (short) 0);
        Arrays.fill(output, (short) 0);
        channel = 0;

        final BundledRedstoneOutputChangedEvent event = new BundledRedstoneOutputChangedEvent(this, -1);
        MinecraftForge.EVENT_BUS.post(event);

        sendData();
    }

    @Override
    public void onEnabled() {
        assert (!getCasing().getCasingWorld().isRemote);

        sendData();
    }

    @Override
    public void onWriteComplete(final Port port) {
        // Start writing again right away to write as fast as possible.
        stepOutput(port);
    }

    @Override
    public void onData(final ByteBuf data) {
        for (int i = 0; i < input.length; i++) {
            input[i] = data.readShort();
        }
        for (int i = 0; i < output.length; i++) {
            output[i] = data.readShort();
        }
        channel = data.readShort();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void render(final boolean enabled, final float partialTicks) {
        if (!isVisible()) {
            return;
        }

        rotateForRendering();

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 0);

        RenderUtil.bindTexture(LOCATION_OVERLAY);

        // Draw base overlay.
        RenderUtil.drawQuad();

        if (!enabled) {
            return;
        }

        RenderUtil.bindTexture(LOCATION_COLORS_OVERLAY);

        // Draw output bar.
        for (int channel = 0; channel < output.length; channel++) {
            if (output[channel] > 0) {
                final float v = channel * V_STEP;
                RenderUtil.drawQuad(0, v, 0.5f, v + V_STEP);
            }
        }

        // Draw input bar.
        for (int channel = 0; channel < input.length; channel++) {
            if (input[channel] > 0) {
                final float v = channel * V_STEP;
                RenderUtil.drawQuad(0.5f, v, 1, v + V_STEP);
            }
        }

        // Draw active channel indicator.
        GlStateManager.disableTexture2D();
        final int color = ColorUtils.getColorByIndex(channel);
        GlStateManager.color(ColorUtils.getRed(color), ColorUtils.getGreen(color), ColorUtils.getBlue(color));
        RenderUtil.drawUntexturedQuad(7 / 16f, 7 / 16f, 2 / 16f, 2 / 16f);
        GlStateManager.enableTexture2D();
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        final int[] outputNbt = nbt.getIntArray(TAG_OUTPUT);
        for (int i = 0; i < outputNbt.length; i++) {
            output[i] = (short) outputNbt[i];
        }

        final int[] inputNbt = nbt.getIntArray(TAG_INPUT);
        for (int i = 0; i < inputNbt.length; i++) {
            input[i] = (short) inputNbt[i];
        }

        channel = (short) Math.max(0, Math.min(input.length - 1, nbt.getShort(TAG_CHANNEL)));
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        final int[] outputNbt = new int[output.length];
        for (int i = 0; i < output.length; i++) {
            outputNbt[i] = output[i];
        }
        nbt.setIntArray(TAG_OUTPUT, outputNbt);

        final int[] inputNbt = new int[input.length];
        for (int i = 0; i < input.length; i++) {
            inputNbt[i] = input[i];
        }
        nbt.setIntArray(TAG_INPUT, inputNbt);

        nbt.setShort(TAG_CHANNEL, channel);
    }

    // --------------------------------------------------------------------- //
    // BundledRedstone

    @Override
    public int getBundledRedstoneOutput(final int channel) {
        return output[channel];
    }

    @Override
    public void setBundledRedstoneInput(final int channel, final short value) {
        // We never call this on the client side, but other might...
        if (getCasing().getCasingWorld().isRemote) {
            return;
        }

        if (value == input[channel]) {
            return;
        }

        input[channel] = value;

        // If the value changed, make sure we're saved.
        getCasing().markDirty();

        // The value changed, cancel our output to make sure it's up-to-date.
        cancelWrite();

        // Update client representation.
        sendData();
    }

    @Override
    public short getBundledRedstoneInput(final int channel) {
        return input[channel];
    }

    // --------------------------------------------------------------------- //

    /**
     * Update the output of the module, pushing a value read from any pipe.
     */
    private void stepOutput(final Port port) {
        final Pipe sendingPipe = getCasing().getSendingPipe(getFace(), port);
        if (!sendingPipe.isWriting()) {
            sendingPipe.beginWrite(input[channel]);
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
            process(receivingPipe.read());

            // Start reading again right away to read as fast as possible.
            receivingPipe.beginRead();
        }
    }

    /**
     * Handle a read value, perform appropriate action.
     *
     * @param value the value that was read.
     */
    private void process(final int value) {
        final short hi = (short) ((value & 0xFF00) >>> 8);
        final short lo = (short) (value & 0xFF);
        if (hi == 0xFF) {
            // lo = new output channel
            if (lo < 0 || lo >= input.length) {
                return;
            }

            if (lo != channel) {
                channel = lo;

                // We changed channel, update what we pass on.
                cancelWrite();

                // Tell our client representation the channel changed.
                sendData();
            }
        } else {
            // hi = channel, lo = value
            setRedstoneOutput(hi, lo);
        }
    }

    /**
     * Update the redstone signal we're outputting.
     *
     * @param channel the channel to change the output of.
     * @param value   the new output value.
     */
    private void setRedstoneOutput(final int channel, final short value) {
        // Ignore invalid channels.
        if (channel < 0 || channel >= output.length) {
            return;
        }

        if (value == output[channel]) {
            return;
        }

        output[channel] = value;

        // If the value changed, make sure we're saved.
        getCasing().markDirty();

        // Notify neighbors, avoid multiple world updates per tick.
        scheduledNeighborUpdate = true;

        // Notify bundled redstone APIs.
        final BundledRedstoneOutputChangedEvent event = new BundledRedstoneOutputChangedEvent(this, channel);
        MinecraftForge.EVENT_BUS.post(event);

        sendData();
    }

    /**
     * Notify all neighbors of a block update, to let them realize our output changed.
     */
    private void notifyNeighbors() {
        scheduledNeighborUpdate = false;
        final Block blockType = getCasing().getCasingWorld().getBlockState(getCasing().getPosition()).getBlock();
        getCasing().getCasingWorld().notifyNeighborsOfStateChange(getCasing().getPosition(), blockType);
    }

    /**
     * Send the current state of the module (to the client).
     */
    private void sendData() {
        final ByteBuf data = Unpooled.buffer();
        for (final short i : input) {
            data.writeShort(input[i]);
        }
        for (final short i : output) {
            data.writeShort(output[i]);
        }
        data.writeShort(channel);
        getCasing().sendData(getFace(), data, DATA_TYPE_UPDATE);
    }
}
