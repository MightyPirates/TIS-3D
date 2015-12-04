package li.cil.tis3d.system.module;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.Pipe;
import li.cil.tis3d.api.Port;
import li.cil.tis3d.api.module.Redstone;
import li.cil.tis3d.api.prefab.AbstractModuleRotatable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class ModuleRedstone extends AbstractModuleRotatable implements Redstone {
    // --------------------------------------------------------------------- //
    // Persisted data

    private int output = 0;
    private int input = 0;

    // --------------------------------------------------------------------- //
    // Computed data

    private static final ResourceLocation LOCATION_OVERLAY = new ResourceLocation(API.MOD_ID, "textures/blocks/overlay/moduleRedstone.png");
    private static final float LEFT_U0 = 9 / 32f;
    private static final float LEFT_U1 = 12 / 32f;
    private static final float RIGHT_U0 = 20 / 32f;
    private static final float RIGHT_U1 = 23 / 32f;
    private static final float SHARED_V0 = 42 / 64f;
    private static final float SHARED_V1 = 57 / 64f;
    private static final float SHARED_W = 3 / 32f;
    private static final float SHARED_H = SHARED_V1 - SHARED_V0;

    // --------------------------------------------------------------------- //

    public ModuleRedstone(final Casing casing, final Face face) {
        super(casing, face);
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        setRedstoneInput(computeRedstoneInput());

        for (final Port port : Port.VALUES) {
            stepOutput(port);
            stepInput(port);
        }
    }

    @Override
    public void onDisabled() {
        input = 0;
        output = 0;

        final Block blockType = getCasing().getWorld().getBlockState(getCasing().getPosition()).getBlock();
        getCasing().markDirty();
        getCasing().getWorld().notifyNeighborsOfStateChange(getCasing().getPosition(), blockType);

        if (!getCasing().getWorld().isRemote) {
            sendData();
        }
    }

    @Override
    public void onEnabled() {
        if (!getCasing().getWorld().isRemote) {
            sendData();
        }
    }

    @Override
    public void onWriteComplete(final Port port) {
        // Start writing again right away to write as fast as possible.
        stepOutput(port);
    }

    @Override
    public void onData(final NBTTagCompound nbt) {
        output = nbt.getInteger("output");
        input = nbt.getInteger("input");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void render(final boolean enabled, final float partialTicks) {
        rotateForRendering();

        RenderHelper.disableStandardItemLighting();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240 / 1.0F, 0 / 1.0F);

        bindTexture(LOCATION_OVERLAY);

        // Draw base overlay.
        drawQuad(0, 0, 1, 0.5f);

        // Draw output bar.
        final float relativeOutput = output / 15f;
        final float heightOutput = relativeOutput * SHARED_H;
        final float v0Output = SHARED_V1 - heightOutput;
        drawQuad(LEFT_U0, (v0Output - 0.5f) * 2f, SHARED_W, heightOutput * 2, LEFT_U0, v0Output, LEFT_U1, SHARED_V1);

        // Draw input bar.
        final float relativeInput = input / 15f;
        final float heightInput = relativeInput * SHARED_H;
        final float v0Input = SHARED_V1 - heightInput;
        drawQuad(RIGHT_U0, (v0Input - 0.5f) * 2f, SHARED_W, heightInput * 2, RIGHT_U0, v0Input, RIGHT_U1, SHARED_V1);

        RenderHelper.enableStandardItemLighting();
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        output = Math.max(0, Math.min(15, nbt.getInteger("output")));
        input = Math.max(0, Math.min(15, nbt.getInteger("input")));
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setInteger("output", output);
        nbt.setInteger("input", input);
    }

    @Override
    public int getRedstoneOutput() {
        return output;
    }

    // --------------------------------------------------------------------- //

    /**
     * Update the known redstone input signal.
     *
     * @param value the new input value.
     */
    private void setRedstoneInput(final int value) {
        if (value == input) {
            return;
        }

        // Clamp to valid redstone range.
        input = Math.max(0, Math.min(15, value));

        if (!getCasing().getWorld().isRemote) {
            // If the value changed, cancel our output to make sure it's up-to-date.
            cancelWrite();

            sendData();
        }
    }

    /**
     * Update the redstone signal we're outputting.
     *
     * @param value the new output value.
     */
    private void setRedstoneOutput(final int value) {
        if (value == output) {
            return;
        }

        // Clamp to valid redstone range.
        output = Math.max(0, Math.min(15, value));

        if (!getCasing().getWorld().isRemote) {
            // If the value changed, notify neighboring blocks and make sure we're saved.
            final Block blockType = getCasing().getWorld().getBlockState(getCasing().getPosition()).getBlock();
            getCasing().markDirty();
            getCasing().getWorld().notifyNeighborsOfStateChange(getCasing().getPosition(), blockType);

            sendData();
        }
    }

    /**
     * Compute the redstone signal we're currently getting from the block in
     * front of the module (i.e. in front of the case on the side of the
     * module's face).
     *
     * @return the current input value.
     */
    private int computeRedstoneInput() {
        final EnumFacing facing = Face.toEnumFacing(getFace());
        final BlockPos inputPos = getCasing().getPosition().offset(facing);
        final int input = getCasing().getWorld().getRedstonePower(inputPos, facing);
        if (input >= 15) {
            return input;
        } else {
            final IBlockState state = getCasing().getWorld().getBlockState(inputPos);
            return Math.max(input, state.getBlock() == Blocks.redstone_wire ? state.getValue(BlockRedstoneWire.POWER) : 0);
        }
    }

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

            // Start reading again right away to read as fast as possible.
            receivingPipe.beginRead();
        }
    }

    /**
     * Send the current state of the module (to the client).
     */
    private void sendData() {
        final NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("output", output);
        nbt.setInteger("input", input);
        getCasing().sendData(getFace(), nbt);
    }
}
