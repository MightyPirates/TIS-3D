package li.cil.tis3d.common.module;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.BundledRedstone;
import li.cil.tis3d.api.module.BundledRedstoneOutputChangedEvent;
import li.cil.tis3d.api.prefab.module.AbstractModuleRotatable;
import li.cil.tis3d.api.util.RenderUtil;
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

    private final int[] output = new int[16];
    private final int[] input = new int[16];
    private int channel = 0;

    // --------------------------------------------------------------------- //
    // Computed data

    // NBT tag names.
    private static final String TAG_OUTPUT = "output";
    private static final String TAG_INPUT = "input";
    private static final String TAG_CHANNEL = "channel";

    // Rendering info.
    private static final ResourceLocation LOCATION_OVERLAY = new ResourceLocation(API.MOD_ID, "textures/blocks/overlay/moduleBundledRedstone.png");
    private static final ResourceLocation LOCATION_COLORS_OVERLAY = new ResourceLocation(API.MOD_ID, "textures/blocks/overlay/moduleBundledRedstoneColors.png");
    private static final float V_STEP = 1 / 16f;

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
    }

    @Override
    public void onDisabled() {
        assert (!getCasing().getCasingWorld().isRemote);

        Arrays.fill(input, 0);
        Arrays.fill(output, 0);
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
    public void onData(final NBTTagCompound nbt) {
        readFromNBT(nbt);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void render(final boolean enabled, final float partialTicks) {
        rotateForRendering();

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 0);

        RenderUtil.bindTexture(LOCATION_OVERLAY);

        // Draw base overlay.
        RenderUtil.drawQuad();

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
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        final int[] outputNbt = nbt.getIntArray(TAG_OUTPUT);
        for (int i = 0; i < outputNbt.length; i++) {
            outputNbt[i] = Math.max(0, Math.min(0xFFFF, outputNbt[i]));
        }
        System.arraycopy(outputNbt, 0, output, 0, Math.min(outputNbt.length, output.length));

        final int[] inputNbt = nbt.getIntArray(TAG_INPUT);
        for (int i = 0; i < inputNbt.length; i++) {
            inputNbt[i] = Math.max(0, Math.min(0xFFFF, inputNbt[i]));
        }
        System.arraycopy(inputNbt, 0, input, 0, Math.min(inputNbt.length, input.length));

        channel = Math.max(0, Math.min(input.length - 1, nbt.getInteger(TAG_CHANNEL)));
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setIntArray(TAG_OUTPUT, output);
        nbt.setIntArray(TAG_INPUT, input);
        nbt.setInteger(TAG_CHANNEL, channel);
    }

    // --------------------------------------------------------------------- //
    // BundledRedstone

    @Override
    public int getBundledRedstoneOutput(final int channel) {
        return output[channel];
    }

    @Override
    public void setBundledRedstoneInput(final int channel, final int value) {
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
        final int hi = (value & 0xFF00) >>> 8;
        final int lo = value & 0xFF;
        if (hi == 0xFF) {
            // lo = new output channel
            if (lo < 0 || lo >= input.length) {
                return;
            }

            if (lo != channel) {
                channel = lo;

                // We changed channel, update what we pass on.
                cancelWrite();
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
    private void setRedstoneOutput(final int channel, final int value) {
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

        // Notify bundled redstone APIs.
        final BundledRedstoneOutputChangedEvent event = new BundledRedstoneOutputChangedEvent(this, channel);
        MinecraftForge.EVENT_BUS.post(event);

        sendData();
    }

    /**
     * Send the current state of the module (to the client).
     */
    private void sendData() {
        final NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);
        getCasing().sendData(getFace(), nbt);
    }
}
